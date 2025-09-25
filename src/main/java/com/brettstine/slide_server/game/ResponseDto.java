package com.brettstine.slide_server.game;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ResponseDto<T> {
    
    private String message;
    private T content;

}
