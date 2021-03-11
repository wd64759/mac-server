package com.cte4.mac.sidecar.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
@ToString(exclude = {"attributes", "rules"})
public class TargetEntity {

    public TargetEntity(String pid) throws IllegalArgumentException {
        this.pid = Integer.parseInt(pid);
    }

    private int pid;
    private String host;
    private long updated;
    private boolean bootweaving;
    private boolean disabled;
    private String agentPort;
    // pid, attached, uptime
    @Getter
    private Map<String, String> attributes = new HashMap<String, String>();
    // rules applied
    @Getter
    private List<RuleEntity> rules = new ArrayList<>();

    public RuleEntity addRule(RuleEntity re) {
        Optional<RuleEntity> existing = rules.stream().filter(rule -> rule.getName().equals(re.getName())).findFirst();
        if (!existing.isPresent()) {
            rules.add(re);
            return re;
        }
        return null;
    }

    public RuleEntity delRule(RuleEntity re) {
        Optional<RuleEntity> existing = rules.stream().filter(rule -> rule.getName().equals(re.getName())).findFirst();
        if (!existing.isPresent()) {
            if (rules.remove(re))
                return re;
        }
        return null;
    }
}
