package com.wan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @author WanYue
 * @date 2024-08-12
 * @description
 *<br>
 * ----------------------2025-03-28改造
 * 修改WebSocket推送消息方式：上一版为记录所有session，推送消息时遍历session发送消息，这样会有性能问题，且无法实现点对点推送
 * 修改为发布订阅模式，创建对应的topic，客户端订阅对应的topic即可接收消息
 * 1.创建WebSocketConfig配置类，继承WebSocketMessageBrokerConfigurer接口,需要启用STOMP协议
 */

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 注册 STOMP 端点，将每个端点映射到特定 URL，以及（可选）
     * 启用和配置 Sockjs 回退选项。
     *
     * @param registry registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //websocket 原生路径
        registry.addEndpoint("/opc-websocket").setAllowedOriginPatterns("*");
        registry
                //websocket 路径且启用 SockJS（在不支持webSocket浏览器中可以自动回退到轮询的方式）
                .addEndpoint("/opc-websocket")
                // 允许跨域
                .setAllowedOriginPatterns("*")
                //启用 SockJS 支持
                .withSockJS()
                //设置 SockJS 流传输的字节限制。当使用 SockJS 的流传输模式时，
                // 服务器会将消息分块发送给客户端。这个方法用于限制每个消息块的最大字节数，
                // 这里设置为 512 * 1024 字节，即 512KB。如果消息块超过这个限制，SockJS 会将消息拆分成多个块进行传输.
                .setStreamBytesLimit(512 * 1024)
                //设置 HTTP 消息缓存的大小。在 SockJS 通信中，服务器会缓存一些 HTTP 消息，以提高性能。
                // 这个方法用于设置缓存中可以存储的最大消息数量，这里设置为 1000 条。
                // 当缓存中的消息数量达到这个限制时，旧的消息会被移除，以腾出空间存储新的消息。
                .setHttpMessageCacheSize(1000);
    }

    /**
     * 配置 Message Broker 选项。
     *
     * @param registry registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置服务端推送消息给客户端的代理路径
        //一对多通道，即客户端订阅路径的前缀信息
        registry.enableSimpleBroker("/topic", "/system");
        //设置一对一推送通道
        registry.setUserDestinationPrefix("/user");
    }
}
