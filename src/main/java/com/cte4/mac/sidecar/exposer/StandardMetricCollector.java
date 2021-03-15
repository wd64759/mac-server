package com.cte4.mac.sidecar.exposer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cte4.mac.sidecar.model.CmdEntity;
import com.cte4.mac.sidecar.model.CmdTypEnum;
import com.cte4.mac.sidecar.model.MetricsEntity;
import com.cte4.mac.sidecar.service.WebSocketFacade;
import com.google.gson.Gson;

import io.prometheus.client.Collector;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class StandardMetricCollector extends Collector implements MetricsCallback {
    private String agentID;
    long timeout = 10000;

    List<MetricFamilySamples> stdMetricsBuf = new ArrayList<MetricFamilySamples>();
    List<String> stdRules = new ArrayList<>();
    List<String> reqToken = new ArrayList<>();

    private static Gson gson = new Gson();

    private StandardMetricCollector(String agentID) {
        this.agentID = agentID;
    }

    private static Map<String, StandardMetricCollector> scRegistry = new ConcurrentHashMap<>();

    public static StandardMetricCollector getInstance(String agentID) {
        StandardMetricCollector instance = scRegistry.get(agentID);
        if (instance != null) {
            return instance;
        }
        synchronized (scRegistry) {
            scRegistry.putIfAbsent(agentID, new StandardMetricCollector(agentID));
            return scRegistry.get(agentID);
        }
    }

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

        log.info("::standard_collector::trigger collect(): - agentID:" + agentID);
        if (stdRules.size() == 0) {
            return new ArrayList<>();
        }

        reqToken.clear();
        long startTime = System.currentTimeMillis();
        WebSocketFacade wsf = WebSocketFacade.getSocketFacade(agentID);
        for (String ruleName : stdRules) {
            CmdEntity ce = new CmdEntity();
            ce.setRuleName(ruleName);
            ce.setCmdType(CmdTypEnum.STD);
            try {
                log.info("::standard_collector::send sync metrics request to target: - cmdEntity:" + ce);
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
        log.info(String.format("data collection completed: %s ms", (endTime - startTime)));
        return copyAndClean(stdMetricsBuf);
    }

    public List<MetricFamilySamples> copyAndClean(List<MetricFamilySamples> metrics) {
        List<MetricFamilySamples> rsList = new ArrayList<>();
        if (metrics.size() != 0) {
            synchronized (metrics) {
                rsList.addAll(metrics);
                metrics.clear();
            }
        }
        return rsList;
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
        if (reqToken.isEmpty()) {
            synchronized (stdMetricsBuf) {
                stdMetricsBuf.notifyAll();
            }
        }
    }

    @Override
    public boolean isAcceptable(MetricsEntity ce) {
        if (ce.getCmdType().equals(CmdTypEnum.STD))
            return true;
        return false;
    }

    @Override
    public String getName() {
        return StandardMetricCollector.class.getName();
    }

    public String getAgentID() {
        return agentID;
    }
}
