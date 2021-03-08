package com.cte4.mac.sidecar.repos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

@Component
public class MetricCallback {
    CollectorRegistry macRegistry = CollectorRegistry.defaultRegistry;
    Map<String, Counter> counterReg = new ConcurrentHashMap<>();

    /**
     * Register and get counter
     * @param name
     * @param tags - TODO: Geneerate MD5 as part of the unique name - e.g. DigestUtils.md5Hex(str)
     * @param helpInfo
     * @return
     */
    public Counter getCounter(String name, String[] tags, String help) {
        Counter c = counterReg.get(name);
        if (c == null) {
            synchronized (counterReg) {
                c = counterReg.get(name);
                if (c == null) {
                    c = Counter.build().name(name).help(help).labelNames(tags).register(macRegistry);
                    counterReg.put(name, c);
                }
            }
        }
        return c;
    }
}
