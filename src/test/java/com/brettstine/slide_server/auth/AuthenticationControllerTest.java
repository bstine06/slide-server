package com.brettstine.slide_server.auth;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    void testRegister() throws Exception {
        String jsonRequest = "{\"username\":\"testUsername\",\"email\":\"test.email@gmail.com\",\"password\":\"password\"}";

        // Mocking the response of the AuthenticationService
        AuthenticationResponse mockResponse = new AuthenticationResponse("mockedJwtToken", null);
        when(authenticationService.register(new RegisterRequest("testUsername", "test.email@gmail.com", "password")))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType("application/json")
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockedJwtToken"));
    }
}

