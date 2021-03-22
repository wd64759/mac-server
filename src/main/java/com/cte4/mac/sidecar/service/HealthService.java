package com.cte4.mac.sidecar.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.cte4.mac.sidecar.exposer.MACHealthCollector;
import com.cte4.mac.sidecar.model.RuleEntity;
import com.cte4.mac.sidecar.model.TargetEntity;
import com.cte4.mac.sidecar.repos.MetricRepository;
import com.cte4.mac.sidecar.utils.MonitorUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class HealthService {

    @Value("${watcher.vm-matcher}")
    private String wtMatcher;
    @Value("${watcher.interval}")
    private long wtInterval;
    @Value("${agent.helper.jar}")
    String helperLoc;
    @Value("${rule.preinstall}")
    String preRules;

    @Autowired
    MetricRepository mRepo;
    @Autowired
    WeavingService weaving;
    @Autowired
    MACHealthCollector mhc;

    @PostConstruct
    public void postInit() {
        startScanDaemon();
    }

    private void startScanDaemon() {
        Thread targetEyes = new Thread(() -> {
            while (true) {
                try {
                    List<String[]> vms = MonitorUtil.getRuntimeVMs(wtMatcher);
                    List<String> currentPID = vms.stream().map(t -> t[0]).collect(Collectors.toList());
                    List<String> regPID = mRepo.getTargets().stream().map(target -> String.valueOf(target.getPid()))
                            .collect(Collectors.toList());
                    List<String> lostPID = new ArrayList<String>();
                    lostPID.addAll(regPID);
                    // remove PID which is down
                    lostPID.removeAll(currentPID);
                    if (!lostPID.isEmpty()) {
                        log.info("detect few proc under-monitoring were down");
                        lostPID.forEach(mRepo::delTarget);
                        lostPID.forEach(WebSocketFacade::cleanListener);
                    }
                    // add new PID into monitoring list
                    currentPID.removeAll(regPID);
                    if (!currentPID.isEmpty()) {
                        log.info("detect some new proc are up to monitor");
                        for (String pID : currentPID) {
                            final TargetEntity te = mRepo.getTarget(pID);
                            boolean validTarget = weaving.attachAgent(te);
                            WebSocketFacade.registerListener(pID, mhc);
                            if (validTarget && te.getAgentPort() != null) {
                                // weaving.attachHelpers(te, helperLoc);
                                weaving.attachHelpersAgent(te, helperLoc);
                                List<RuleEntity> preinstalledRules = getPreRules(Arrays.asList(preRules.split(",")));
                                if (preinstalledRules.size() > 0) {
                                    preinstalledRules.forEach(t -> {
                                        try {
                                            weaving.applyRule(te, t);
                                        } catch (RuleInjectionException e) {
                                            log.error("fail to apply pre-install rule:" + t);
                                        }
                                    });
                                }
                            } else {
                                String rulename = "MACHEALTH_FNC";
                                Optional<RuleEntity> find = mRepo.getRules().stream()
                                        .filter(rule -> rule.getName().equals(rulename)).findFirst();
                                if (find.isPresent()) {
                                    RuleEntity re = find.get().clone();
                                    weaving.applyListener(te, re);
                                    te.addRule(re);
                                }
                                te.setDisabled(true);
                            }
                        }
                    }
                    try {
                        Thread.sleep(wtInterval * 1000);
                    } catch (InterruptedException e) {
                    }
                } catch (Exception e) {
                    log.error("unknown exception:", e);
                }
            }
        });
        targetEyes.setDaemon(true);
        targetEyes.start();
    }

    private List<RuleEntity> getPreRules(List<String> ruleNames) {
        List<RuleEntity> finds = mRepo.getRules().stream().filter(rule -> ruleNames.contains(rule.getName()))
                .collect(Collectors.toList());
        return finds;
    }

    public static void main(String[] args) {
        List<String[]> l = new ArrayList<>();
        l.add(new String[] { "1", "app1" });
        l.add(new String[] { "2", "app2" });
        l.add(new String[] { "3", "app3" });
        List<String> pidList = l.stream().map(t -> t[0]).collect(Collectors.toList());

        if (pidList.removeAll(Arrays.asList(new String[] { "2", "5" }))) {
            pidList.forEach(t -> log.info(t));
        }

    }
}
