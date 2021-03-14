package com.cte4.mac.sidecar.exposer;

import com.cte4.mac.sidecar.model.MetricsEntity;

public interface MetricsCallback {
    public String getName();
    public void callback(MetricsEntity cmdEntity);
    public boolean isAcceptable(MetricsEntity ce);
}
