package com.brettstine.slide_server.user;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private final UserService userService;

    public UserProfileService(UserService userService) {
        this.userService = userService;
    }

    public boolean isAuthorizedToAccessProfile(String authenticatedUsername, String requestedUsername) {
        return authenticatedUsername.equals(requestedUsername);
    }

    public UserProfileResponse getUserProfile(String username) {
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return UserProfileResponse.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .currentGameId(user.getCurrentGameId())
            .build();
    }
}

