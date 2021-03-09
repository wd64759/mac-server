package com.cte4.mac.sidecar.repos;

import java.util.List;
import java.util.Optional;

import com.cte4.mac.sidecar.model.MetricEntity;
import com.cte4.mac.sidecar.utils.MonitorUtil;

public interface MetricHandlerInterface {

    public void processMetric(MetricEntity me);
    
    /**
     * the meter unique key = ruleName + MD5(tag names)
     * @param me
     * @return
     */
    public default String getMeterKey(MetricEntity me) {
        List<String> soredKeys = MonitorUtil.sortedKeys(me.getTags().keySet());
        StringBuilder sBuilder = new StringBuilder();
        soredKeys.forEach((k)->{
            sBuilder.append(k);
        });
        String md5 = MonitorUtil.getMD5(sBuilder.toString());
        String meterKey = String.format("%s-%s", Optional.ofNullable(me.getRuleName()).orElse("emptyrule"), md5);
        return meterKey;
    }
}
