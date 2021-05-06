package com.cte4.mac.server.exposer;

import com.cte4.mac.server.model.MetricsEntity;

public interface MetricsCallback {
    public String getName();
    public void callback(MetricsEntity cmdEntity);
    public boolean isAcceptable(MetricsEntity ce);
}
