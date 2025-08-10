package com.michaeldavidsim.ratedrps_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.michaeldavidsim.ratedrps_server.websocket.GameWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameWebSocketHandler gameWebSocketHandler;
    private final AuthHandshakeInterceptor authHandshakeInterceptor;

    public WebSocketConfig(GameWebSocketHandler gameWebSocketHandler,
                           AuthHandshakeInterceptor authHandshakeInterceptor) {
        this.gameWebSocketHandler = gameWebSocketHandler;
        this.authHandshakeInterceptor = authHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameWebSocketHandler, "/ws/game")
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOrigins("https://ratedrps.vercel.app", "http://localhost:5173");
    }
}
