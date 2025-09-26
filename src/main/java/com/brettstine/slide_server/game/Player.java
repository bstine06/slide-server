package com.brettstine.slide_server.game;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Player {

    private final String username;
    @Builder.Default
    private boolean ready = false;
    @Builder.Default
    private int level = 0;
    @Builder.Default
    private int x = 0;
    @Builder.Default
    private int y = 0;
    @Builder.Default
    private float vx = 0;
    @Builder.Default
    private float vy = 0;
    @Builder.Default
    private Direction nextMove = null;
    @Builder.Default
    private Integer stopX = null;
    @Builder.Default
    private Integer stopY = null;
    @Builder.Default
    private String color = "#FF0000";

}
