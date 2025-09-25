package com.brettstine.slide_server.websocket.dto;

import java.util.UUID;

import com.brettstine.slide_server.game.Direction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerUpdatePayload {
    private String username;
    private int level;
    private int x;
    private int y;
    private float vx;
    private float vy;
    private Direction nextMove;
    private int stopX;
    private int stopY;

}
