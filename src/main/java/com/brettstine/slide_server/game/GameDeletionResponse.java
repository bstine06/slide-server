package com.brettstine.slide_server.game;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameDeletionResponse {
    
    private String message;
    private UUID gameId;

}
