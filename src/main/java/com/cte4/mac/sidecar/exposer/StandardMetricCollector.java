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
        if (stdRules.size() == 0 && !stdMetricsBuf.isEmpty()) {
            log.info("abandon metrics: " + stdMetricsBuf.size());
            stdMetricsBuf.clear();
            return new ArrayList<>();
        }
        // log.info("::metric-collector::standard::scan - send request for: " + agentID);
        WebSocketFacade wsf = WebSocketFacade.getSocketFacade(agentID);
        if (wsf == null) {
            log.info(String.format("::metric-collector::standard::skip - rules:%s, is_ws_ready:%s", stdRules.size(), wsf!=null));
            return new ArrayList<>();
        }
        synchronized (reqToken) {
            reqToken.clear();
        }
        long startTime = System.currentTimeMillis();
        for (String ruleName : stdRules) {
            CmdEntity ce = new CmdEntity();
            ce.setRuleName(ruleName);
            ce.setCmdType(CmdTypEnum.STD);
            try {
                // log.info("::standard_collector::send sync metrics request to target: - cmdEntity:" + ce);
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
        log.info(String.format("data collection completed. timecost: %s ms, metrics size: %s", (endTime - startTime), stdMetricsBuf.size()));
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

    public void addRule(String rule) {
        stdRules.add(rule);
    }

    public void removeRule(String rule) {
        stdRules.remove(rule);
    }

    public boolean isRuleEmpty() {
        return stdRules.size() == 0;
    }

    /**
     * watch against ws
     * 
     * @param me
     */
    @Override
    public void callback(MetricsEntity me) {
        stdMetricsBuf.addAll(me.getMetrics());
        boolean goFlag = false;
        synchronized (reqToken) {
            reqToken.remove(me.getRuleName());
            if (reqToken.isEmpty()) {
                goFlag = true;
            }
        }
        if (goFlag) {
            // log.info(String.format("data collection ack."));
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
