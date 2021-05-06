package com.cte4.mac.server.exposer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cte4.mac.server.model.CmdTypEnum;
import com.cte4.mac.server.model.MetricsEntity;
import com.cte4.mac.server.model.TargetEntity;
import com.cte4.mac.server.repos.MetricRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.prometheus.client.Collector.MetricFamilySamples;

@Component
public class MACHealthCollector implements MetricsCallback {

    @Autowired
    MetricRepository mRepo;

    @Override
    public String getName() {
        return MACHealthCollector.class.getName();
    }

    Map<String, String> targetPorts = new HashMap<>();

    @Override
    public void callback(MetricsEntity cmdEntity) {
        List<TargetEntity> targets = mRepo.getTargets().stream().filter(t -> t.getAgentPort() == null)
                .collect(Collectors.toList());
        if (targets.size() > 0) {
            refreshTargetPorts(cmdEntity);
            for (TargetEntity te : targets) {
                String agentPort = targetPorts.get(String.valueOf(te.getPid()));
                te.setAgentPort(agentPort);
            }
        }
    }

    /**
     * Refresh target port from message
     * @param cmdEntity
     */
    private void refreshTargetPorts(MetricsEntity cmdEntity) {
        List<MetricFamilySamples> metrics = cmdEntity.getMetrics();
        for (MetricFamilySamples mfs : metrics) {
            for (MetricFamilySamples.Sample sample : mfs.samples) {
                String pid = sample.labelValues.get(sample.labelNames.indexOf("pid"));
                String agentID = sample.labelValues.get(sample.labelNames.indexOf("agentPort"));
                targetPorts.put(pid, agentID);
            }
        }
    }

    @Override
    public boolean isAcceptable(MetricsEntity ce) {
        if (ce.getCmdType().equals(CmdTypEnum.FUNC) && ce.getRuleName().equals("MACHEALTH_FNC")) {
            return true;
        }
        return false;
    }

}
