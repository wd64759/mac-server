package com.cte4.mac.sidecar.repos;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.cte4.mac.sidecar.model.MetricEntity;
import com.cte4.mac.sidecar.model.RuleEntity;
import com.cte4.mac.sidecar.model.TargetEntity;

import org.springframework.stereotype.Component;

@Component
public class MetricRepository {

    // All regristed rules
    Map<String, RuleEntity> rules = new ConcurrentHashMap<String, RuleEntity>();
    // Metrics feed from target process
    Map<String, MetricEntity> metrics = new ConcurrentHashMap<String, MetricEntity>();
    // Target process under monitoring, key = PID
    Map<String, TargetEntity> targets = new ConcurrentHashMap<String, TargetEntity>();

    public MetricEntity putMetric(String key, MetricEntity metric) {
        return metrics.put(key, metric);
    }

    public MetricEntity getMetric(String key) {
        return metrics.get(key);
    }

    public TargetEntity getTarget(String key) {
        if (key != null && !key.isBlank()) {
            TargetEntity te = targets.get(key);
            if (te == null) {
                te = new TargetEntity(key);
                targets.put(key, te);
            }
            return te;
        }
        return null;
    }

    public TargetEntity delTarget(String key) {
        if (key != null && !key.isBlank()) {
            return targets.remove(key);
        }
        return null;
    }

    @PostConstruct
    private void initRepo() {
        this.initRules();
    }

    private void initRules() {
        RuleEntity re = new RuleEntity("JVMMetric");
        re.setDisabled(false);
        re.setVersion(1);
        // TODO: add monitoring script
        re.setScript("TODO");
        rules.put(re.getName(), re);
    }

    public List<RuleEntity> getRules() {
        return rules.values().stream().collect(Collectors.toList());
    }

    public List<MetricEntity> getMetrics() {
        return metrics.values().stream().collect(Collectors.toList());
    }

    public List<TargetEntity> getTargets() {
        return targets.values().stream().collect(Collectors.toList());
    }
}
