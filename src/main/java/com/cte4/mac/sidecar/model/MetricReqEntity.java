package com.cte4.mac.sidecar.model;

import lombok.Data;

@Data
public class MetricReqEntity {
    String meter;
    String metric;
    String attibutes;
    String values;
    long timestamp;
    String context;
}
