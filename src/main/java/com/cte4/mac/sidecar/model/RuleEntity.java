package com.cte4.mac.sidecar.model;

import lombok.Data;

@Data
public class RuleEntity {
    private String name;
    private boolean disabled;
    private String script;
    private int version;
}