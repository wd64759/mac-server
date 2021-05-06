package com.cte4.mac.server.repos;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.cte4.mac.server.model.MeterEnum;

import org.springframework.stereotype.Component;

@Component
public class MetricHandlerBuilder {

    /**
     * the meter repository
     */
    Map<String, MetricHandlerInterface> metersRegstry = new ConcurrentHashMap<>();

    
    public MetricHandlerBuilder() {
        metersRegstry.put(MeterEnum.COUNTER.name(), new DefaultCounterHandler());
        metersRegstry.put(MeterEnum.GAUGE.name(), new DefaultGaugeHandler());
    }
    /**
     * 
     * @param meterType
     * @param rule
     * @return
     */
    public MetricHandlerInterface getMetricHandler(String meterType) throws MeterNotFoundException {
        return Optional.ofNullable(metersRegstry.get(meterType)).orElseThrow(()->new MeterNotFoundException());
    }

    
}
