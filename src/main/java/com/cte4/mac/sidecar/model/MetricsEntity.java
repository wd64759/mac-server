package com.cte4.mac.sidecar.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.prometheus.client.Collector.MetricFamilySamples;

/**
 * All models sent from applications are metrics format
 */
public class MetricsEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    public MetricsEntity() {
        this.timestamp = System.currentTimeMillis();
    }
    
    private String ruleName;
    private CmdTypEnum cmdType;  
    // Message generation timestamp
    private long timestamp;
    // Matric content
    private List<MetricFamilySamples> metrics = new ArrayList<>();

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setCmdType(CmdTypEnum cmdTyp) {
        this.cmdType = cmdTyp;
    }

    public CmdTypEnum getCmdType() {
        return cmdType;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public List<MetricFamilySamples> getMetrics() {
        return metrics;
    }

}
