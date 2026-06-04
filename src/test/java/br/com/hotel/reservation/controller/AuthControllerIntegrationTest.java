package br.com.hotel.reservation.controller;

import br.com.hotel.reservation.BaseIntegrationTest;
import br.com.hotel.reservation.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldRegisterAndReturnTokens() throws Exception {
        final var request = new RegisterRequest("New User", "newuser@hotel.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        final var registerRequest = new RegisterRequest("Login User", "loginuser@hotel.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(registerRequest)));

        final var loginBody = "{\"email\":\"loginuser@hotel.com\",\"password\":\"password123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        final var registerRequest = new RegisterRequest("Refresh User", "refreshuser@hotel.com", "password123");
        final var registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerRequest)))
                .andReturn();

        final var refreshToken = MAPPER.readTree(registerResult.getResponse().getContentAsString())
                .get("refreshToken").asText();

        final var refreshBody = "{\"refreshToken\":\"" + refreshToken + "\"}";

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void shouldReturnNotFoundForInvalidRefreshToken() throws Exception {
        final var refreshBody = "{\"refreshToken\":\"invalid-token-that-does-not-exist\"}";

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenRegisterWithBlankEmail() throws Exception {
        final var invalidBody = "{\"name\":\"User\",\"email\":\"\",\"password\":\"password123\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }
}
