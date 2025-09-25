package com.brettstine.slide_server.user;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brettstine.slide_server.config.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final UserProfileService userProfileService;
    private final UserService userService;

    @GetMapping("/check/username")
    public ResponseEntity<CredentialsCheckResponse> checkUsernameAvailability(
        @RequestParam("username") String username
    ) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CredentialsCheckResponse.builder()
                    .isAvailable(false)
                    .message("username not provided")
                    .build()
                );
        }
        logger.info("Received request to check username availability on username {}", username);
        boolean isAvailable = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(CredentialsCheckResponse.builder()
                    .isAvailable(isAvailable)
                    .message("username '" + username + "' is " + ((isAvailable) ? "" : "not ") + "available")
                    .build());
    }

    @GetMapping("/check/email")
    public ResponseEntity<CredentialsCheckResponse> checkEmailAvailability(
        @RequestParam("email") String email
    ) {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CredentialsCheckResponse.builder()
                    .isAvailable(false)
                    .message("email not provided")
                    .build()
            );
        }
        logger.info("Received request to check email availability on email {}", email);
        boolean isAvailable = userService.isEmailAvailable(email);
        return ResponseEntity.ok(CredentialsCheckResponse.builder()
                    .isAvailable(isAvailable)
                    .message("email '" + email + "' is " + ((isAvailable) ? "" : "not ") + "available")
                    .build());
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getUser(@PathVariable("username") String username, Authentication authentication) {
        
        String authenticatedUsername = authentication.getName();

        // Delegate the authorization check to the service
        if (!userProfileService.isAuthorizedToAccessProfile(authenticatedUsername, username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Fetch the user profile using the service
        UserProfileResponse response = userProfileService.getUserProfile(username);

        return ResponseEntity.ok(response);
    }
    

}
