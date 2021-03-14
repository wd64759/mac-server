package com.cte4.mac.sidecar.model;

import com.google.gson.annotations.Expose;

import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
@ToString(exclude = {"script"})
public class RuleEntity implements Cloneable {
    public RuleEntity(String name) {
        this.name = name;
        this.version = 1;
        this.disabled = false;
    }
    private String name;
    private boolean disabled;

    @Expose(serialize = false)
    private String script;
    private int version;

    public RuleEntity clone() {
        RuleEntity copy = null;
        try {
            copy = (RuleEntity) super.clone();
        } catch (CloneNotSupportedException exception) {
            log.error("unable to clone the rule entity", exception);
        } 
        return copy;
    }
}