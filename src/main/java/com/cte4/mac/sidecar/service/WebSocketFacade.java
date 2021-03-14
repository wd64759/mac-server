package com.cte4.mac.sidecar.service;

import java.io.IOException;
import java.util.HashMap;
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

import com.cte4.mac.sidecar.exposer.MetricsCallback;
import com.cte4.mac.sidecar.model.MeterEnum;
import com.cte4.mac.sidecar.model.MetricEntity;
import com.cte4.mac.sidecar.model.MetricReqEntity;
import com.cte4.mac.sidecar.model.MetricsEntity;
import com.cte4.mac.sidecar.repos.MetricHandlerBuilder;
import com.google.gson.Gson;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@ServerEndpoint(value = "/websocket/{agent}")
@Log4j2
public class WebSocketFacade {

    private String agentID;
    private long onlineTS;
    private Session session;

    private static Gson gson = new Gson();
    private static Map<String, WebSocketFacade> clients = new ConcurrentHashMap<>();
    private Map<String, MetricsCallback> listeners = new HashMap<>();

    @OnOpen
    public void onOpen(@PathParam("agent") String agentID, Session session) throws IOException {
        log.info(String.format(">>>>>>  agent:%s is online", agentID));
        this.agentID = agentID;
        this.onlineTS = System.currentTimeMillis();
        this.session = session;
        clients.put(agentID, this);
    }

    public void sendMessage(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        log.info("message:" + message);
        try {
            MetricsEntity me = gson.fromJson(message, MetricsEntity.class);
            me = validAndEnrich(me);

            for(Map.Entry<String, MetricsCallback> entry: listeners.entrySet()) {
                MetricsCallback mcb = entry.getValue();
                if(mcb.isAcceptable(me)) {
                    mcb.callback(me);
                }
            }
        } catch (Exception e) {
            log.error("fail to process message from websocket", e);
            throw new IOException(e);
        }
    }

    /**
     * with more client informaiton we can put some generic tags
     * 
     * @param me
     * @throws InvalidReqestException
     */
    private MetricsEntity validAndEnrich(MetricsEntity me) throws InvalidReqestException {
        Optional.ofNullable(me.getMetrics()).orElseThrow(() -> new InvalidReqestException());
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
        // try {
        // session.getBasicRemote().sendText("server error:" + e.getMessage());
        // } catch (IOException e1) {
        // }
    }

    /**
     * Resgister message listener against specific agent
     * @param agent
     * @param callback
     */
    public static void addCallback(String agent, MetricsCallback callback) {
        WebSocketFacade wsf = clients.get(agent);
        if (wsf == null) {
            log.info(String.format("ws for %s does not exist"), agent);
            return;
        }
        wsf.listeners.put(callback.getName(), callback);
    }

    /**
     * Remove message listener by name
     * @param agent
     * @param callback
     */
    public static void removeCallback(String agent, MetricsCallback callback) {
        WebSocketFacade wsf = clients.get(agent);
        if (wsf == null) {
            log.info(String.format("ws for %s does not exist"), agent);
            return;
        }
        wsf.listeners.remove(callback.getName());
    }

    public long getDuration() {
        return System.currentTimeMillis() - this.onlineTS;
    }

}
