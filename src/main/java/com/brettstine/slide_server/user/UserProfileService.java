package com.brettstine.slide_server.user;

import java.util.regex.Pattern;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private final UserService userService;
    private static final Pattern HEX_COLOR_PATTERN =
        Pattern.compile("^#(?:[0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$");

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
            .color(user.getColor())
            .currentGameId(user.getCurrentGameId())
            .build();
    }

    public UserProfileResponse updateUserColor(String authenticatedUsername, String hex) {
        User user = userService.findByUsername(authenticatedUsername)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String cleanedHex = hex.trim();

        if (!HEX_COLOR_PATTERN.matcher(cleanedHex).matches()) {
            throw new IllegalArgumentException("Hex color code is malformed");
        }

        String normalizedHex;
        if (cleanedHex.length() == 4) { // # + 3 chars
            char r = cleanedHex.charAt(1);
            char g = cleanedHex.charAt(2);
            char b = cleanedHex.charAt(3);
            normalizedHex = String.format("#%c%c%c%c%c%c",
                r, r, g, g, b, b);
        } else {
            normalizedHex = cleanedHex;
        }

        user.setColor(normalizedHex);
        userService.save(user);

        return UserProfileResponse.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .color(user.getColor())
            .currentGameId(user.getCurrentGameId())
            .build();

    }
}

