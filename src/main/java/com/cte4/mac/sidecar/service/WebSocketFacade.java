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

import com.cte4.mac.sidecar.model.MetricEntity;
import com.cte4.mac.sidecar.repos.MetricHandlerBuilder;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@ServerEndpoint(value = "/websocket/{agent}")
@Log4j2
public class WebSocketFacade {

    @Autowired 
    MetricHandlerBuilder metricCallback;

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
            MetricEntity me = gson.fromJson(message, MetricEntity.class);
            validAndEnrich(me);
            metricCallback.getMetricHandler(me.getMeterType()).processMetric(me);;
        } catch (Exception e) {
            log.error("fail to process message from websocket", e);
            throw new IOException(e);
        }
    }

    /**
     * put some general fields as tags
     * @param me
     * @throws InvalidReqestException
     */
    private void validAndEnrich(MetricEntity me) throws InvalidReqestException {
        Optional.ofNullable(me.getMeterType()).orElseThrow(()->new InvalidReqestException());
        me.getTags().putIfAbsent("updatedTime", Long.toString(me.getUpdated()==0?System.currentTimeMillis():me.getUpdated()));
        return;
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        log.info(String.format(">>>>>>  agent:%s is offline, duration:%ss", agentID, this.getDuration()/1000));
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
