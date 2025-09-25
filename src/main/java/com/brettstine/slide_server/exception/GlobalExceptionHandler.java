package com.brettstine.slide_server.exception;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.brettstine.slide_server.config.JwtAuthenticationFilter;
import com.brettstine.slide_server.game.ResponseDto;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ResponseDto<Object>> handleUserNotFound(UsernameNotFoundException ex) {
        ResponseDto<Object> body = new ResponseDto<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(UnauthorizedProfileAccessException.class)
    public ResponseEntity<ResponseDto<Object>> handleUnauthorized(UnauthorizedProfileAccessException ex) {
        ResponseDto<Object> body = new ResponseDto<>("Unauthorized access to this profile.", null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseDto<Object>> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("Bad credentials: {}", ex.getMessage());
        ResponseDto<Object> body = new ResponseDto<>("Invalid username or password.", null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        ResponseDto<Object> body = new ResponseDto<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseDto<Object>> handleEntityNotFound(EntityNotFoundException ex) {
        ResponseDto<Object> body = new ResponseDto<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseDto<Object>> handleIllegalState(IllegalStateException ex) {
        ResponseDto<Object> body = new ResponseDto<>(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseDto<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType() == UUID.class) {
            return ResponseEntity.badRequest().body(
                new ResponseDto<>("Invalid format. Must be a valid UUID.", null)
            );
        }
        return ResponseEntity.badRequest().body(
            new ResponseDto<>("Invalid argument type", null)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Object>> handleOtherExceptions(Exception ex) {
        logger.warn(ex.getMessage());
        ResponseDto<Object> body = new ResponseDto<>("Something went wrong.", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
