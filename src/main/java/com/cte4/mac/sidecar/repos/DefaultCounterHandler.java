package com.cte4.mac.sidecar.repos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cte4.mac.sidecar.model.MetricEntity;
import com.cte4.mac.sidecar.utils.MonitorUtil;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

public class DefaultCounterHandler implements MetricHandlerInterface {

    CollectorRegistry promRegistry = CollectorRegistry.defaultRegistry;
    Map<String, Counter> counterRegistery = new ConcurrentHashMap<>();

    @Override
    public void processMetric(MetricEntity me) {
        List<String> orderedTags = MonitorUtil.sortedKeys(me.getTags().keySet());
        Counter meter = getCounter(this.getMeterKey(me), orderedTags, "");
        meter.inc();
    }

    
    /**
     * Register and get counter
     * @param name
     * @param tags
     * @param helpInfo
     * @return
     */
    public Counter getCounter(String name, List<String> tags, String help) {
        Counter c = counterRegistery.get(name);
        if (c == null) {
            synchronized (counterRegistery) {
                c = counterRegistery.get(name);
                if (c == null) {
                    c = Counter.build().name(name).help(help).labelNames(tags.toArray(new String[0])).register(promRegistry);
                    counterRegistery.put(name, c);
                }
            }
        }
        return c;
    }
}
