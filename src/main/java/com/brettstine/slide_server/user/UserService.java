package com.brettstine.slide_server.user;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.brettstine.slide_server.auth.RegisterRequest;
import com.brettstine.slide_server.config.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(RegisterRequest request) {
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail().toLowerCase().trim())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.USER)
            .build();
        try {
            user = repository.save(user);
            logger.info("Created user {} and stored to database", user.getUsername());
            return user;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Username or email already exists");
        }
    }

    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public boolean isUsernameAvailable(String username) {
        return !repository.existsByUsername(username);
    }

    public boolean isEmailAvailable(String email) {
        return !repository.existsByEmailIgnoreCase(email);
    }

    public void clearCurrentGameIdForUsername(String username) {
        User user = repository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setCurrentGameId(null);
        repository.save(user);
    }

    public User save(User user) {
        return repository.save(user);
    }
}

