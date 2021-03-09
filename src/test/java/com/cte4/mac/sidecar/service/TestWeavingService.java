package com.cte4.mac.sidecar.service;

import java.util.List;

import com.cte4.mac.sidecar.model.TargetEntity;
import com.cte4.mac.sidecar.repos.MetricRepository;
import com.cte4.mac.sidecar.utils.MonitorUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class TestWeavingService {
    @Autowired
    WeavingService weaving;
    @Autowired
    MetricRepository repo;

    private String targetPort;
    private String targetName;

    @BeforeEach
    private void init() {
        List<String[]> process = MonitorUtil.getRuntimeVMs("cte4");
        this.targetPort = process.get(0)[0];
        this.targetName = process.get(0)[1];
    }

    @Test
    public void attachAgent() throws AgentAttachException {
        log.info(String.format(">>%s,%s", this.targetName, this.targetPort));
        TargetEntity te = repo.getTarget(this.targetPort);
        weaving.attachAgent(te);
    }

}
