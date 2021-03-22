package com.cte4.mac.sidecar.exposer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cte4.mac.sidecar.model.CmdTypEnum;
import com.cte4.mac.sidecar.model.MetricsEntity;

import io.prometheus.client.Collector;
import lombok.extern.log4j.Log4j2;

/**
 * Each agent has only ONE function data collector for function releated metrics
 */
@Log4j2
public class FunctionMetricCollector extends Collector implements MetricsCallback {

    private static Map<String, FunctionMetricCollector> fcRegisery = new ConcurrentHashMap<>();
    private String agentID;
    List<String> fncRules = new ArrayList<>();

    private FunctionMetricCollector(String agentID) {
        this.agentID = agentID;
    }

    public static FunctionMetricCollector getInstance(String agentID) {
        FunctionMetricCollector instance = fcRegisery.get(agentID);
        if (instance != null) {
            return instance;
        }

        synchronized (fcRegisery) {
            if (instance == null) {
                instance = new FunctionMetricCollector(agentID);
                fcRegisery.put(agentID, instance);
            }
            return instance;
        }
    }

    /**
     * temp repository before Promethues pull them
     */
    List<MetricFamilySamples> buffer = new ArrayList<>();
    private int BUFFER_MAX = 10000;

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> rs = new ArrayList<>();
        if (buffer.size() > 0) {
            log.info("::metric-collector::function::scan - buffer size: " + buffer.size());
            synchronized (buffer) {
                rs.addAll(buffer);
                buffer.clear();
            }
        }
        return rs;
    }

    @Override
    public String getName() {
        return FunctionMetricCollector.class.getName();
    }

    @Override
    public void callback(MetricsEntity cmdEntity) {
        synchronized (buffer) {
            try {
                if (buffer.size() >= BUFFER_MAX) {
                    List<MetricFamilySamples> last100 = buffer.subList(buffer.size() - 101, buffer.size() - 1);
                    buffer.clear();
                    buffer.addAll(last100);
                }
                buffer.addAll(cmdEntity.getMetrics());              
            } catch (Exception e) {
                log.error("fail to add msg to collection buffer", e);
            }
        }
    }

    @Override
    public boolean isAcceptable(MetricsEntity ce) {
        if (ce.getCmdType().equals(CmdTypEnum.FUNC))
            return true;
        return false;
    }

    public String getAgentID() {
        return agentID;
    }

    public void addRule(String rule) {
        fncRules.add(rule);
    }

    public void removeRule(String rule) {
        fncRules.remove(rule);
    }

    public boolean isRuleEmpty() {
        return fncRules.size() == 0;
    }

}
