package com.brettstine.slide_server.auth;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.brettstine.slide_server.user.User;
import com.brettstine.slide_server.user.UserService;
import com.brettstine.slide_server.config.JwtService;
import com.brettstine.slide_server.user.Role;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationServiceTest {

    @Mock
    private UserService userService; // Mock UserService

    @Mock
    private JwtService jwtService; // Mock JwtService

    @InjectMocks
    private AuthenticationService authenticationService; // Inject mocks into the service

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize the mocks

        // Initialize the test data
        registerRequest = new RegisterRequest("testUsername", "test.email@gmail.com", "password");
        user = User.builder()
            .username(registerRequest.getUsername())
            .email(registerRequest.getEmail())
            .password("encodedPassword")
            .role(Role.USER)
            .build();
    }

    @Test
    void testRegister() {
        // Prepare mock input for registering
        RegisterRequest request = new RegisterRequest("user@example.com", "username", "password");

        // Mock the UserService to return the user object
        when(userService.createUser(any(RegisterRequest.class))).thenReturn(user);

        // Mock the JwtService to return a fixed token
        String mockedToken = "mocked.jwt.token";  // This is a mocked token
        when(jwtService.generateToken(any(User.class))).thenReturn(mockedToken);

        // Call the register method
        AuthenticationResponse response = authenticationService.register(request);

        // Assert that the response is not null and contains the expected mocked token
        assertNotNull(response);
        assertEquals(mockedToken, response.getToken());  // Verify that the response contains the mocked token
    }
}
