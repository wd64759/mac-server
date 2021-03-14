package com.cte4.mac.sidecar.exposer;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

import com.cte4.mac.sidecar.model.CmdEntity;
import com.cte4.mac.sidecar.model.CmdTypEnum;
import com.cte4.mac.sidecar.model.MetricsEntity;
import com.cte4.mac.sidecar.service.WebSocketFacade;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.prometheus.client.Collector;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class StandardMetricCollector extends Collector implements MetricsCallback {

    @Autowired
    WebSocketFacade wsf;

    long timeout = 10000;

    List<MetricFamilySamples> stdMetricsBuf = new ArrayList<MetricFamilySamples>();
    List<String> stdRules = new ArrayList<>();
    List<String> reqToken = new ArrayList<>();

    private static Gson gson = new Gson();

    /**
     * When prometheus pull action trigger it. The request will be sent to target
     * through websocket
     */
    @Override
    public List<MetricFamilySamples> collect() {
        // mfs values should retrive from the target service via WebSocket
        // (I need to design some way to evaluate the request before sending it out )
        // List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
        // RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        // mfs.add(new GaugeMetricFamily("demo_start_time_seconds", "Start time of the
        // process since unix epoch in seconds.",
        // runtimeBean.getStartTime() / MILLISECONDS_PER_SECOND));
        // log.info("run TargetMetricCollector.collect()");
        // return mfs;
        reqToken.clear();
        long startTime = System.currentTimeMillis();
        for (String ruleName : stdRules) {
            CmdEntity ce = new CmdEntity();
            ce.setRuleName(ruleName);
            ce.setCmdType(CmdTypEnum.DATA);
            try {
                wsf.sendMessage(gson.toJson(ce));
                reqToken.add(ruleName);
            } catch (IOException e) {
                log.error("fail to send request", e);
            }
        }
        // hold-on until get response
        synchronized (stdMetricsBuf) {
            try {
                stdMetricsBuf.wait(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        log.info("data collection completed: %s ms" + (endTime - startTime));
        return stdMetricsBuf;
    }

    public void addStdRule(String rule) {
        stdRules.add(rule);
    }

    /**
     * watch against ws
     * 
     * @param me
     */
    @Override
    public void callback(MetricsEntity me) {
        boolean reqMatched = reqToken.remove(me.getRuleName());
        if (!reqMatched) {
            log.info("unknown metrics - %s" + me);
        }
        if(reqToken.isEmpty()) {
            synchronized(stdMetricsBuf) {
                stdMetricsBuf.notifyAll();
            }
        }
    }

    @Override
    public boolean isAcceptable(MetricsEntity ce) {
        if (ce.getCmdType().equals(CmdTypEnum.DATA))
            return true;
        return false;
    }

    @Override
    public String getName() {
        return StandardMetricCollector.class.getName();
    }

}
