package com.brettstine.slide_server.websocket;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.brettstine.slide_server.config.JwtService;
import com.brettstine.slide_server.game.GameService;
import com.brettstine.slide_server.user.UserService;

import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Component
@AllArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final UserService userService;
    private final GameService gameService;

    @Override
    public boolean beforeHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) throws Exception {

        String query = request.getURI().getQuery(); // e.g., token=...&gameId=1234
        if (query == null) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        String token = null;
        String gameIdParam = null;

        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                token = param.substring("token=".length());
            } else if (param.startsWith("gameId=")) {
                gameIdParam = param.substring("gameId=".length());
            }
        }

        if (token == null || gameIdParam == null) {
            // reject if either is missing
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        try {
            // validate token
            String username = jwtService.extractUsername(token);
            userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Unknown user"));
            attributes.put("username", username);

            // validate gameId
            UUID gameId = UUID.fromString(gameIdParam); // throws if invalid format
            attributes.put("gameId", gameId);

            // check if gameId actually exists in DB
            // this throws if the game doesnt exist and blocks the websocket from mounting
            gameService.getById(gameId);

            // check if user is in the game
            // this throws if user doesnt exist in game and blocks the websocket from mounting
            gameService.confirmPlayerExistsInGame(gameId, username);

            return true;

        } catch (Exception e) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false; // invalid token or bad gameId
        }
    }

    @Override
    public void afterHandshake(
        @NonNull ServerHttpRequest request,
        @NonNull ServerHttpResponse response,
        @NonNull WebSocketHandler wsHandler,
        @Nullable Exception exception) {
        // no-op
    }
}