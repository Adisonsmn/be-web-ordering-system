package com.aromasenja.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Konfigurasi WebSocket STOMP.
 *
 * - Broker: in-memory di /topic (cukup untuk scope project ini)
 * - Endpoint: /ws dengan SockJS fallback
 * - Application prefix: /app (pesan dari client ke server)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // In-memory broker untuk broadcast ke semua subscriber topic
        config.enableSimpleBroker("/topic");
        // Prefix untuk pesan dari client ke server (message mapping)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // Izinkan origin dari config + semua port localhost untuk dev
                .setAllowedOriginPatterns(allowedOrigins, "http://localhost:*", "https://*.supabase.co")
                .withSockJS(); // Fallback untuk browser yang tidak support native WebSocket
    }
}
