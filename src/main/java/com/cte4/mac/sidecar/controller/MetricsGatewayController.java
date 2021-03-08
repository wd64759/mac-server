package com.cte4.mac.sidecar.controller;

import com.cte4.mac.sidecar.repos.MetricCallback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.prometheus.client.Counter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Api
@RestController
public class MetricsGatewayController {

    @Autowired
    MetricCallback mc;
    
    @ApiOperation(value = "trigger sampler counter once", notes = "here is notes")
    @GetMapping(value = "/mtgateway/counter/{name}")
    public void sampleCounter(@PathVariable String name) {
        log.info("call sample counter - inc : " + name);
        Counter counter = mc.getCounter(name, new String[]{"dummy"}, "sample counter");
        counter.labels("dummy-value").inc();
    }
}
