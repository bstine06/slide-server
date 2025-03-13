package com.brettstine.slide_server.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.jsonwebtoken.security.SignatureException;

@ExtendWith(SpringExtension.class)  // To enable Spring test support
@TestPropertySource(properties = "secretkey=DZ68IVSJ54IEPYRZ2KQ42BQRZSZ1O6T384M6XPL63ZB1GAUK88LWSDY9DNGFJSNP")  // Inject your secret key here
class JwtServiceTest {

    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    @Value("${secretkey}")
    private String secretKey;  // Automatically injected from @TestPropertySource

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService(secretKey);  // Replace "secretkey" with your actual key
        userDetails = User.withUsername("testUser")
                          .password("testPassword")
                          .authorities("USER")
                          .build();
    }

    @Test
    void testGenerateToken() {
        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertTrue(token.startsWith("eyJhbGciOiJIUzI1Ni"));  // Check if it starts with a valid JWT header
    }

    @Test
    void testExtractUsername() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("testUser", username);
    }

    @Test
    void testIsTokenExpired_ExpiredToken() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Override the expiration date in the token to the past (for testing)
        JwtService spyJwtService = spy(jwtService);
        doReturn(new Date(System.currentTimeMillis() - 1000)).when(spyJwtService).extractExpiration(any());

        // Act & Assert
        assertTrue(spyJwtService.isTokenExpired(token));
    }

    @Test
    void testIsTokenExpired_ValidToken() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act & Assert
        assertFalse(jwtService.isTokenExpired(token));  // Token shouldn't be expired right after creation
    }

    @Test
    void testIsTokenValid_ValidToken() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act & Assert
        assertTrue(jwtService.isTokenValid(token, userDetails));  // Should be valid
    }

    @Test
void testIsTokenValid_InvalidToken() {
    // Generate a valid token with the correct secret key
    String validToken = jwtService.generateToken(userDetails);

    // Create a second JwtService with a different secret key to simulate an invalid token
    JwtService invalidJwtService = new JwtService("HGFNCQPH7S7TOPI7ZINCT3RZJMB9PCMLJM3WSPBRCPLSFNH2VTNWEP3Q2V4VJ9ZR");

    // Step 3: Assert that the token is invalid when checked with the different secret key
    try {
        boolean isValid = invalidJwtService.isTokenValid(validToken, userDetails);
        assertFalse(isValid); // Assert that the token is invalid
    } catch (SignatureException e) {
        // You can log or assert if necessary
        assertTrue(e.getMessage().contains("JWT signature does not match"));
    }
}

}
