package com.brettstine.slide_server.user;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brettstine.slide_server.config.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final UserService userService;

    @PostMapping("/check/username")
    public ResponseEntity<Boolean> checkUsernameAvailability(
        @RequestBody Map<String, String> body
    ) {
        String username = body.get("username");
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(false);
        }
        logger.info("Received request to check username availability on username {}", username);
        boolean isAvailable = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(isAvailable);
    }

    @PostMapping("/check/email")
    public ResponseEntity<Boolean> checkEmailAvailability(
        @RequestBody Map<String, String> body
    ) {
        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(false);
        }
        logger.info("Received request to check email availability on email {}", email);
        boolean isAvailable = userService.isEmailAvailable(email);
        return ResponseEntity.ok(isAvailable);
    }

}
