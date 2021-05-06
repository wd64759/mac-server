package com.cte4.mac.server.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = {"tags", "context"})
public class MetricEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    String ruleName;
    long updated;
    Map<String, String> tags = new HashMap<>();
    Map<String, String> context = new HashMap<>();
    String meterType;
    long metric;
}
