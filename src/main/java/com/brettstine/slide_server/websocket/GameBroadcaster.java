package com.brettstine.slide_server.websocket;

import com.brettstine.slide_server.websocket.dto.WebSocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class GameBroadcaster {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<UUID, Map<String, WebSocketSession>> gameSessions = new ConcurrentHashMap<>();

    public void register(UUID gameId, String username, WebSocketSession session) {
        gameSessions.computeIfAbsent(gameId, k -> new ConcurrentHashMap<>()).put(username, session);
    }

    public void unregister(UUID gameId, String username) {
        Map<String, WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions != null) {
            sessions.remove(username);
            if (sessions.isEmpty()) gameSessions.remove(gameId);
        }
    }

    public void broadcast(UUID gameId, WebSocketMessage<?> message) {
        Map<String, WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions == null) return;

        sessions.values().forEach(session -> {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void sendTo(UUID gameId, String username, WebSocketMessage<?> message) {
        Map<String, WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions == null) return;

        WebSocketSession session = sessions.get(username);
        if (session == null) return;

        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeSession(UUID gameId, String username, int statusCode, String reason) {
        Map<String, WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions == null) return;

        WebSocketSession session = sessions.get(username);
        if (session != null && session.isOpen()) {
            try {
                session.close(new CloseStatus(statusCode, reason));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void closeAllSessionsForGame(UUID gameId, int statusCode, String reason) {
        Map<String, WebSocketSession> sessions = gameSessions.get(gameId);
        if (sessions == null) return;

        sessions.values().stream().forEach((WebSocketSession session) -> {
            try {
                session.close(new CloseStatus(statusCode, reason));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }

}
