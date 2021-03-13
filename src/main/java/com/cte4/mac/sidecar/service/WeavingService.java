package com.cte4.mac.sidecar.service;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.sun.tools.attach.*;

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
                // props:
                String[] props = new String[]{};
                Install.install(String.valueOf(te.getPid()), te.isBootweaving(), te.getHost(), agentPort, props);
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
            String result = submit.addJarsToSystemClassloader(Arrays.asList(jarLoc));
            log.info(String.format("MAC helpers are attached, result:%s", result));
            // List<String> jars = submit.getLoadedSystemClassloaderJars();
            // jars.forEach(log::info);
        } catch (Exception e) {
            log.error(String.format("fail to attach helper for %s", te));
            throw new HelperAttachException(e);
        }
        return true;
    }

    /**
     * Attach helpers as agent model
     * @param te
     * @param jarLoc
     * @return
     * @throws HelperAttachException
     */
    public boolean attachHelpersAgent(@NonNull TargetEntity te, String jarLoc) throws HelperAttachException {
        try {
            log.info(String.format("attach %s with %s", te.getPid(), jarLoc));
            VirtualMachine vm = VirtualMachine.attach(String.valueOf(te.getPid()));
            vm.loadAgent(jarLoc);
            vm.detach();
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
        log.info(String.format("apply rule [target:%s], [rule:%s]", te, re.getName()));
        try {
            Submit submit = Optional.ofNullable(getAgentHandler(te)).orElseThrow(()->new RuleInjectionException(WARNING_AGENT_DOWN));
            ByteArrayInputStream is = new ByteArrayInputStream(re.getScript().getBytes());
            String result = submit.addRulesFromResources(Arrays.asList(is));
            te.addRule(re.clone());
            log.info(String.format("rule applied, result:%s", result));
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
    public boolean detachRule(TargetEntity te, String[] ruleNameArr) {
        log.info(String.format("TODO: detach [target:%s], [rule:%s]", te, Arrays.asList(ruleNameArr)));
        try {
            Submit submit = Optional.ofNullable(getAgentHandler(te)).orElseThrow(()->new RuleDetachException(WARNING_AGENT_DOWN));
            List<RuleEntity> targetRules = te.getRules().stream().filter(o->Arrays.stream(ruleNameArr).anyMatch(t->t == o.getName())).collect(Collectors.toList());
            for(RuleEntity re:targetRules) {
                if(!re.isDisabled()) {
                    String result = submit.deleteRulesFromResources(Arrays.asList(new ByteArrayInputStream(re.getScript().getBytes())));
                    re.setDisabled(true);
                    log.info(String.format("rule[%s] applied, result:%s", re.getName(), result));
                }
            }
        } catch (Exception e) {
            log.error("fail to detach the rule", e);
        }
        return true;
    }
}
