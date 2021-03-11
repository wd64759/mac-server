package com.cte4.mac.sidecar.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

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

    @Autowired
    MetricRepository mRepo;
    @Autowired
    WeavingService weaving;

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
                    }
                    // add new PID into monitoring list
                    currentPID.removeAll(regPID);
                    if (!currentPID.isEmpty()) {
                        log.info("detect some new proc are up to monitor");
                        for (String pID : currentPID) {
                            TargetEntity te = mRepo.getTarget(pID);
                            weaving.attachAgent(te);
                            if (te.getAgentPort() != null) {
                                weaving.attachHelpers(te, helperLoc);
                            } else {
                                log.warn("disable the agent for proc as it's unreachable:" + te);
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
