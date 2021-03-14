package com.cte4.mac.sidecar.exposer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import io.prometheus.client.Collector;

@Component
public class FunctionMetricCollector extends Collector {

    /**
     * temp repository before Promethues pull them
     */
    List<MetricFamilySamples> buffer = new ArrayList<>();

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> rs = new ArrayList<>();
        if (buffer.size() > 0) {
            synchronized(buffer) {
                rs.addAll(buffer);
                buffer.clear();
            }
        }
        return rs;
    }

}
