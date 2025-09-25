package com.brettstine.slide_server.websocket;

import com.brettstine.slide_server.gameplay.GameplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final GameMessageBroker messageBroker;
    private final GameBroadcaster broadcaster;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        String username = (String) session.getAttributes().get("username");
        UUID gameId = (UUID) session.getAttributes().get("gameId");
        if (username == null || gameId == null) return;

        broadcaster.register(gameId, username, session);
        messageBroker.sendInitialState(gameId, username);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        String username = (String) session.getAttributes().get("username");
        UUID gameId = (UUID) session.getAttributes().get("gameId");
        if (username == null || gameId == null) return;

        messageBroker.handleIncoming(gameId, username, message.getPayload());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String username = (String) session.getAttributes().get("username");
        UUID gameId = (UUID) session.getAttributes().get("gameId");
        if (username != null && gameId != null) {
            broadcaster.unregister(gameId, username);
        }
    }
}

