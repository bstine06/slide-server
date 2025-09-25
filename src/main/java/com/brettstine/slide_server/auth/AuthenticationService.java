package com.brettstine.slide_server.auth;

import com.brettstine.slide_server.config.JwtAuthenticationFilter;
import com.brettstine.slide_server.config.JwtService;
import com.brettstine.slide_server.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.brettstine.slide_server.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        User user = userService.createUser(request);
        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
            .token(jwtToken)
            .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        logger.info("Authentication request received for user {}", request.getUsername());

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(), 
                    request.getPassword()
                )
            );
            logger.info("Authentication successful for user: {}", request.getUsername());
        } catch (Exception e) {
            logger.info("Authentication failed: {}", (e.getClass().getSimpleName() + " - " + e.getMessage()));
            throw e;
        }

        User user = userService.findByUsername(request.getUsername())
            .or(() -> userService.findByEmail(request.getUsername())) //user may pass email instead
            .orElseThrow();
        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
            .token(jwtToken)
            .build();
    }
}

