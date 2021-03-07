package com.cte4.mac.sidecar.service;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.cte4.mac.sidecar.model.RuleEntity;
import com.cte4.mac.sidecar.model.TargetEntity;

import org.jboss.byteman.agent.install.Install;
import org.jboss.byteman.agent.submit.Submit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.SocketUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class WeavingService {

    private static final String WARNING_AGENT_DOWN = "monitoring agent is not up (or down)";

    @Value("${agent.port.range}")
    String portRange;

    /**
     * Attach the monitoring agent
     * @param te
     * @throws AgentAttachException
     */
    public void attachAgent(TargetEntity te) throws AgentAttachException {
        try {
            if (te.getAgentPort() == null || te.getAgentPort().isBlank()) {
                if(Install.isAgentAttached(String.valueOf(te.getPid()))) {
                    log.info(String.format("monitoring agent is already attached for target:%s", te));
                    return;
                }
                int agentPort = findAvaliablePort();
                Install.install(String.valueOf(te.getPid()), te.isBootweaving(), te.getHost(), agentPort,
                        new String[] {});
                te.setAgentPort(String.valueOf(agentPort));
                log.info(String.format("attach the monitoring agent at port:%s, succssfully for target:%s", agentPort,
                        te));
            }
        } catch (Exception e) {
            log.error(String.format("fail to attach agent to the target process - PID:%s", te.getPid()), e);
            throw new AgentAttachException(e);
        }
    }

    /**
     * Attach helpers jar
     * @param te
     * @param jarLoc
     */
    public boolean attachHelpers(@NonNull TargetEntity te, String jarLoc) throws HelperAttachException {
        try {
            Submit submit = Optional.ofNullable(getAgentHandler(te)).orElseThrow(()->new HelperAttachException(WARNING_AGENT_DOWN));
            submit.addJarsToSystemClassloader(Arrays.asList(jarLoc));
        } catch (Exception e) {
            log.error(String.format("fail to attach helper for %s", te));
            throw new HelperAttachException(e);
        }
        return true;
    }

    /**
     * get avaliable port
     * 
     * @return
     */
    protected int findAvaliablePort() {
        String[] ports = portRange.split(",");
        int agentPort = Submit.DEFAULT_PORT;
        try {
            agentPort = SocketUtils.findAvailableTcpPort(Integer.valueOf(ports[0]), Integer.valueOf(ports[1]));
        } catch (Exception e) {
            log.warn(String.format("fail to find avaliable port by the given port range %s, try to use default port:%s",
                    portRange, Submit.DEFAULT_PORT), e);
        }
        return agentPort;
    }

    /**
     * Attach rule to target process
     * @param te
     * @param re
     * @return
     * @throws RuleInjectionException
     */
    public boolean applyRule(TargetEntity te, RuleEntity re) throws RuleInjectionException {
        log.info(String.format("apply rule [target:%s], [rule:%s]", te, re));
        try {
            Submit submit = Optional.ofNullable(getAgentHandler(te)).orElseThrow(()->new RuleInjectionException(WARNING_AGENT_DOWN));
            submit.submitRequest(re.getScript());
        } catch (Exception e) {
            log.error(String.format("fail to apply the rule for target:%s", te), e);
            throw new RuleInjectionException(e);
        }
        return true;
    }

    /**
     * retrive the agent handler
     * @param te
     * @return
     */
    protected Submit getAgentHandler(TargetEntity te) {
        Submit agentHandler = null;
        try {
            if (te.getAgentPort() == null) {
                log.warn(String.format("monitoring agent for PID:%s is not up", te.getPid()));
            }
            agentHandler = new Submit(te.getHost(), Integer.valueOf(te.getAgentPort()));
        } catch (Exception e) {
            log.error(String.format("fail to get agent handler for process %s", te), e);
        }
        return agentHandler;
    }

    /**
     * Detach rules
     * @param te
     * @param re
     * @return
     */
    public boolean detachRule(TargetEntity te, String[] specificRules) {
        log.info(String.format("TODO: detach [target:%s], [rule:%s]", te, specificRules));
        try {
            Submit submit = Optional.ofNullable(getAgentHandler(te)).orElseThrow(()->new RuleDetachException(WARNING_AGENT_DOWN));
            List<RuleEntity> targetRules = te.getRules().stream().filter(rule->Arrays.stream(specificRules).anyMatch(t->t.equals(rule))).collect(Collectors.toList());
            for(RuleEntity re:targetRules) {
                if(!re.isDisabled()) {
                    submit.deleteRulesFromResources(Arrays.asList(new ByteArrayInputStream(re.getScript().getBytes())));
                }
            }
        } catch (Exception e) {
            log.error("fail to detach the rule", e);
        }
        return true;
    }
}
