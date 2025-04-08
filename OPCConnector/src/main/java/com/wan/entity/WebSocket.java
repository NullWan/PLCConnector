package com.wan.entity;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.websocket.WsRemoteEndpointAsync;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WanYue
 * @date 2024-08-12
 * @description
 */
@Deprecated
@Component
@Log4j2
//@ServerEndpoint("/websocket/{clientId}")
public class WebSocket {

    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();


    /**
     * 连接建立时触发
     *
     * @param session  session
     * @param clientId 客户端id
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("clientId") String clientId) {
        log.info("【WebSocket消息】有新的连接，客户端id为：{}", clientId);
        SESSION_MAP.putIfAbsent(clientId, session);
    }

    /**
     * 连接关闭时触发
     *
     * @param session session
     */
    @OnClose
    public void onClose(Session session) {
        log.info("【WebSocket消息】连接断开");
    }

    /**
     * 收到客户端消息时触发
     *
     * @param message 消息
     */
    @OnMessage
    public void onMessage(String message) {
        log.info("【WebSocket消息】收到客户端消息：" + message);
    }

    /**
     * 发生错误时触发
     *
     * @param session session
     * @param error   错误
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("【WebSocket消息】发生错误", error);
    }

    /**
     * 发送消息
     *
     * @param clientId 客户端id
     * @param message  消息
     */
    public void sendMessageSync(String clientId, String message) {
        Session session = SESSION_MAP.get(clientId);
        if (checkState(session, clientId)) {
            try {
                session.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                log.error("【WebSocket消息】发送消息失败", e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 异步发送消息
     *
     * @param clientId 客户端id
     * @param message  消息
     */
    public void sendMessageAsync(String clientId, String message) {
        Session session = SESSION_MAP.get(clientId);
        if (checkState(session, clientId)) {
            WsRemoteEndpointAsync remoteEndpoint = (WsRemoteEndpointAsync) session.getAsyncRemote();
            remoteEndpoint.sendText(message, sendResult -> {
                if (!sendResult.isOK()) {
                    log.error("【WebSocket消息】发送消息失败,客户端: {}", clientId);
                }
            });
        }
    }

    /**
     * 检查状态
     *
     * @param session  session
     * @param clientId 客户端id
     * @return true false
     */
    private Boolean checkState(Session session, String clientId) {
        if (session == null) {
            log.error("【WebSocket消息】发送消息失败，客户端id不存在");
            return false;
        }
        if (!session.isOpen()) {
            log.warn("【WebSocket消息】发送消息失败，客户端连接已关闭,连接关闭");
            SESSION_MAP.remove(clientId);
            return false;
        }
        return true;
    }

    /**
     * 关闭连接
     *
     * @param clientId 客户端id
     */
    public void close(String clientId) {
        if (!SESSION_MAP.containsKey(clientId)) {
            log.warn("【WebSocket消息】关闭连接失败，客户端id不存在");
            return;
        }
        Session session = SESSION_MAP.get(clientId);
        try {
            session.close();
            SESSION_MAP.remove(clientId);
        } catch (IOException e) {
            log.error("【WebSocket消息】关闭连接失败", e);
            throw new RuntimeException(e);
        }
    }
}
