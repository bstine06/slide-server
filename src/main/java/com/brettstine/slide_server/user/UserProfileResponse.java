package com.brettstine.slide_server.user;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {

    private String username;
    private String email;
    private UUID currentGameId;
    private String color;

}
