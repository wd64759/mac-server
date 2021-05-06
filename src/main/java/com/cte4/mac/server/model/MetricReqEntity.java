package com.cte4.mac.server.model;

import lombok.Data;

@Data
public class MetricReqEntity {
    private String name;
    private String meter;
    private String metric;
    private String attibutes;
    private String values;
    private long timestamp;
    private String context;
}
