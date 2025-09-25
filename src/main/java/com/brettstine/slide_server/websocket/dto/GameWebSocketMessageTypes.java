package com.brettstine.slide_server.websocket.dto;

public enum GameWebSocketMessageTypes {
    
    GAME_STATE,
    PLAYER_READY,
    PLAYER_LEAVE,
    GAME_START,
    PLAYER_UPDATE,
    ERROR, 
    PLAYER_JOIN

}
