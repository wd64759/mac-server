package com.cte4.mac.sidecar.exposer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cte4.mac.sidecar.model.CmdTypEnum;
import com.cte4.mac.sidecar.model.MetricsEntity;

import io.prometheus.client.Collector;

/**
 * Each agent has only ONE function data collector for function releated metrics
 */
public class FunctionMetricCollector extends Collector implements MetricsCallback {

    private static Map<String, FunctionMetricCollector> fcRegisery = new ConcurrentHashMap<>();
    private String agentID;

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

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> rs = new ArrayList<>();
        if (buffer.size() > 0) {
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
            buffer.addAll(cmdEntity.getMetrics());
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

}
