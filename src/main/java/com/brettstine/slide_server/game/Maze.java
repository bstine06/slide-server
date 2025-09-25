package com.brettstine.slide_server.game;

import lombok.Data;

@Data
public class Maze {
    
    private int[][] board;
    private int startX;
    private int startY;
    private int finishX;
    private int finishY;

}
