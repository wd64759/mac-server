package com.cte4.mac.sidecar.repos;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.cte4.mac.sidecar.model.MetricEntity;
import com.cte4.mac.sidecar.model.RuleEntity;
import com.cte4.mac.sidecar.model.TargetEntity;
import com.cte4.mac.sidecar.utils.MonitorUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class MetricRepository {

    @Value("${rule.func.scripts}")
    String RULE_FUNC_SCRIPTS;

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
        URL loc = ClassLoader.getSystemResource(RULE_FUNC_SCRIPTS);
        File folder = new File(loc.getPath());
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".btm");
            }
        });
        Arrays.asList(files).forEach(f->{
            String fname = f.getName();
            String ruleName = (fname.substring(0,fname.lastIndexOf(".")));
            RuleEntity re = new RuleEntity(ruleName);
            String ruleScript = String.format("rulescripts/%s.btm", ruleName);
            re.setScript(MonitorUtil.loadRuleScript(ruleScript));
            rules.put(re.getName(), re);
        });
        rules.forEach(log::info);
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
