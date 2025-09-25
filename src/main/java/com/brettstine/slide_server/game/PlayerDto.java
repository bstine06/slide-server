package com.brettstine.slide_server.game;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerDto {

    private String username;
    private boolean ready;
    private int level;
    private int x;
    private int y;
    private float vx;
    private float vy;
    private Direction nextMove;
    private int stopX;
    private int stopY;

    public static PlayerDto convertToDto(Player player) {
        PlayerDto dto = PlayerDto.builder()
                            .username(player.getUsername())
                            .ready(player.isReady())
                            .level(player.getLevel())
                            .x(player.getX())
                            .y(player.getY())
                            .build();
        return dto;
    }

}
