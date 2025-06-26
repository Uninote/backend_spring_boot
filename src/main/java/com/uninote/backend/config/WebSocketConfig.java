package com.uninote.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        logger.debug("Configuring message broker...");
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        logger.debug("Message broker configured with topics: /topic, /queue and app prefix: /app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        logger.debug("Registering STOMP endpoints...");
        
        // Add SockJS endpoint
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000", "http://localhost:8080", "https://uninote.gr", "file://", "*")
                .withSockJS();
        logger.debug("Registered SockJS endpoint: /ws with allowed origins: http://localhost:3000, http://localhost:8080, https://uninote.gr, file://");
        
        // Add raw WebSocket endpoint
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000", "http://localhost:8080", "https://uninote.gr", "file://", "*");
        logger.debug("Registered raw WebSocket endpoint: /ws with allowed origins: http://localhost:3000, http://localhost:8080, https://uninote.gr, file://");
        
        // Add specific questionnaire endpoint
        registry.addEndpoint("/ws/questionnaire")
                .setAllowedOrigins("http://localhost:3000", "http://localhost:8080", "https://uninote.gr", "file://", "*")
                .withSockJS();
        logger.debug("Registered questionnaire SockJS endpoint: /ws/questionnaire");
        
        registry.addEndpoint("/ws/questionnaire")
                .setAllowedOrigins("http://localhost:3000", "http://localhost:8080", "https://uninote.gr", "file://", "*");
        logger.debug("Registered questionnaire raw WebSocket endpoint: /ws/questionnaire");
        
        logger.debug("STOMP endpoints registration completed");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024) // 64KB
                   .setSendBufferSizeLimit(512 * 1024) // 512KB
                   .setSendTimeLimit(20000); // 20 seconds
        logger.debug("WebSocket transport configured with size limits");
    }
}
