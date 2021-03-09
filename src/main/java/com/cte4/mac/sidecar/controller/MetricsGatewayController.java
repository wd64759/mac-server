package com.cte4.mac.sidecar.controller;

import com.cte4.mac.sidecar.model.MeterEnum;
import com.cte4.mac.sidecar.model.MetricEntity;
import com.cte4.mac.sidecar.repos.MeterNotFoundException;
import com.cte4.mac.sidecar.repos.MetricHandlerBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    MetricHandlerBuilder mc;
    
    @ApiOperation(value = "post info to counter meter with the given name", notes = "here is notes")
    @PostMapping(value = "/mtgateway/counter/{name}")
    public String counterMeter(@PathVariable String name) {
        log.info("call sample counter - inc : " + name);
        MetricEntity me = new MetricEntity();
        me.setMeterType(MeterEnum.COUNTER.name());
        me.getTags().put("dummy", "sample counter");
        String response = "ok";
        
        try {
            mc.getMetricHandler(me.getMeterType()).processMetric(me);
        } catch (MeterNotFoundException e) {
            response = "failed";
            log.error("fail to test counter function", e);
        }
        // Counter counter = mc.getCounter(name, new String[]{"dummy"}, "sample counter");
        // counter.labels("dummy-value").inc();
        return response;
    }

    @ApiOperation(value = "trigger sampler counter once", notes = "here is notes")
    @PostMapping(value = "/mtgateway/gauge/{name}")
    public String gaugeMeter(@PathVariable String name, String body) {
        return "ok";
    }

}
