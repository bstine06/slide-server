package com.brettstine.slide_server.websocket.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerReadyPayload {
    private String username;
    private boolean ready;
}