package com.cte4.mac.server.model;

import java.io.Serializable;

public class CmdEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    private String ruleName;
    private CmdTypEnum cmdType;
    public long timestamp;

    public CmdEntity() {
        timestamp = System.currentTimeMillis();
    }

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
        return timestamp;
    }

}
