package com.brettstine.slide_server.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage<T> {

    private GameWebSocketMessageTypes type; // e.g., PLAYER_MOVE, PLAYER_READY, GAME_STATE
    private T payload;
    private long timestamp;
    
}

