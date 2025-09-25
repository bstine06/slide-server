package com.brettstine.slide_server.exception;

public class UnauthorizedProfileAccessException extends RuntimeException {
    
    public UnauthorizedProfileAccessException() {
        super("Unauthorized access to this profile.");
    }

    public UnauthorizedProfileAccessException(String message) {
        super(message);
    }
}
