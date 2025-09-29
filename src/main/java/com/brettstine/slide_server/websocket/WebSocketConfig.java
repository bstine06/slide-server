package com.brettstine.slide_server.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameWebSocketHandler gameWebSocketHandler;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Value("${frontend.url}")
    private String frontendUrl;

    public WebSocketConfig(
        GameWebSocketHandler gameWebSocketHandler,
        JwtHandshakeInterceptor jwtHandshakeInterceptor
    ) {
        this.gameWebSocketHandler = gameWebSocketHandler;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(gameWebSocketHandler, "/ws/game")
                .addInterceptors(jwtHandshakeInterceptor) // <-- JWT auth
                .setAllowedOrigins(frontendUrl); // adjust origin in prod
    }
}

