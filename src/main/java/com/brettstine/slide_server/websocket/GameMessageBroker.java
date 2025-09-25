package com.brettstine.slide_server.websocket;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.brettstine.slide_server.gameplay.GameplayService;
import com.brettstine.slide_server.websocket.dto.GameWebSocketMessageTypes;
import com.brettstine.slide_server.websocket.dto.WebSocketMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import com.brettstine.slide_server.websocket.dto.*;

@Component
@RequiredArgsConstructor
public class GameMessageBroker {

    private final GameplayService gameplayService;
    private final GameBroadcaster broadcaster;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendInitialState(UUID gameId, String username) {
        gameplayService.sendFullGameStateToPlayer(gameId, username);
    }

    public void handleIncoming(UUID gameId, String username, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String typeStr = root.get("type").asText();
            JsonNode payloadNode = root.get("payload");

            GameWebSocketMessageTypes type;
            try {
                type = GameWebSocketMessageTypes.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                sendError(gameId, username, "Invalid message type: " + typeStr);
                return;
            }

            switch (type) {
                case PLAYER_READY -> {
                    PlayerReadyPayload ready = objectMapper.treeToValue(payloadNode, PlayerReadyPayload.class);
                    gameplayService.readyUpPlayer(gameId, username, ready);
                }
                case PLAYER_LEAVE -> {
                    PlayerNameOnlyPayload leave = objectMapper.treeToValue(payloadNode, PlayerNameOnlyPayload.class);
                    gameplayService.removePlayer(gameId, username, leave);
                }
                case PLAYER_UPDATE -> {
                    PlayerUpdatePayload move = objectMapper.treeToValue(payloadNode, PlayerUpdatePayload.class);
                    gameplayService.updatePlayer(gameId, username, move);
                }
                default -> sendError(gameId, username, "Unsupported message type: " + type);
            }

        } catch (Exception e) {
            sendError(gameId, username, "Failed to process message: " + e.getMessage());
        }
    }

    private void sendError(UUID gameId, String username, String errorMsg) {
        WebSocketMessage<String> error = new WebSocketMessage<>(
                GameWebSocketMessageTypes.ERROR,
                errorMsg,
                System.currentTimeMillis()
        );
        broadcaster.sendTo(gameId, username, error);
    }
}


