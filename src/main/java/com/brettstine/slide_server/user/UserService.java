package com.brettstine.slide_server.user;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.brettstine.slide_server.auth.RegisterRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(RegisterRequest request) {
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.USER)
            .build();
        try {
            return repository.save(user);
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
        return !repository.existsByEmail(email);
    }
}

