package com.cte4.mac.sidecar.service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.cte4.mac.sidecar.model.MeterEnum;
import com.cte4.mac.sidecar.model.MetricEntity;
import com.cte4.mac.sidecar.model.MetricReqEntity;
import com.cte4.mac.sidecar.repos.MetricHandlerBuilder;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@ServerEndpoint(value = "/websocket/{agent}")
@Log4j2
public class WebSocketFacade {

    // @Autowired
    MetricHandlerBuilder metricCallback = new MetricHandlerBuilder();

    private String agentID;
    private long onlineTS;
    private Session session;

    private static Gson gson = new Gson();
    private static Map<String, WebSocketFacade> clients = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(@PathParam("agent") String agentID, Session session) throws IOException {
        log.info(String.format(">>>>>>  agent:%s is online", agentID));
        this.agentID = agentID;
        this.onlineTS = System.currentTimeMillis();
        this.session = session;
        clients.put(agentID, this);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        log.info("message:" + message);
        try {
            MetricReqEntity mr = gson.fromJson(message, MetricReqEntity.class);
            MetricEntity me = validAndEnrich(mr);
            metricCallback.getMetricHandler(me.getMeterType()).processMetric(me);
        } catch (Exception e) {
            log.error("fail to process message from websocket", e);
            throw new IOException(e);
        }
    }

    /**
     * put some general fields as tags
     * 
     * @param me
     * @throws InvalidReqestException
     */
    private MetricEntity validAndEnrich(MetricReqEntity mr) throws InvalidReqestException {
        Optional.ofNullable(mr.getMeter()).orElseThrow(() -> new InvalidReqestException());
        MetricEntity me = new MetricEntity();
        if (mr.getAttibutes() != null) {
            String[] tagNameArr = mr.getAttibutes().split(",");
            String[] tagValArr = mr.getValues().split(",");
            if (tagNameArr.length != tagValArr.length) {
                throw new InvalidReqestException("tags length is incorrect.");
            }
            for (int i = 0; i < tagNameArr.length; i++) {
                me.getTags().put(tagNameArr[i], tagValArr[i]);
            }
        }
        me.getTags().putIfAbsent("updatedTime",
                Long.toString(me.getUpdated() == 0 ? System.currentTimeMillis() : me.getUpdated()));
        me.setMeterType("counter".equalsIgnoreCase(mr.getMeter())?MeterEnum.COUNTER.name():MeterEnum.GAUGE.name());
        me.setMetric(Long.valueOf(mr.getMetric()));
        me.setRuleName(mr.getName());
        return me;
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        log.info(String.format(">>>>>>  agent:%s is offline, duration:%ss", agentID, this.getDuration() / 1000));
        clients.remove(agentID);
    }

    @OnError
    public void onError(Session session, Throwable e) {
        log.info("get unexpected error", e);
        try {
            session.getBasicRemote().sendText("server error:" + e.getMessage());
        } catch (IOException e1) {
        }
    }

    public long getDuration() {
        return System.currentTimeMillis() - this.onlineTS;
    }

}
