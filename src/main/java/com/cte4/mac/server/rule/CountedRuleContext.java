package com.cte4.mac.server.rule;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class CountedRuleContext {
    private String clazzName;
    private String methodName;
    private List<String> params;
    private List<Map<String, String>> tags;
}
