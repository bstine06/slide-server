package com.brettstine.slide_server.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CredentialsCheckResponse {

    private Boolean isAvailable;
    @Builder.Default
    private String message = "";

}
