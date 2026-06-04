package br.com.hotel.reservation.service;

import br.com.hotel.reservation.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secretKey", "test-secret-key-for-unit-tests-only");
    }

    @Test
    void shouldGenerateAccessToken() {
        final var user = User.builder()
                .id(1L)
                .email("user@test.com")
                .name("Test User")
                .password("encoded")
                .build();

        final var token = tokenService.generateAccessToken(user);

        assertThat(token).isNotBlank();
    }

    @Test
    void shouldVerifyValidToken() {
        final var user = User.builder()
                .id(1L)
                .email("user@test.com")
                .name("Test User")
                .password("encoded")
                .build();

        final var token = tokenService.generateAccessToken(user);
        final var subject = tokenService.verifyToken(token);

        assertThat(subject).isEqualTo("user@test.com");
    }

    @Test
    void shouldReturnEmptyStringForInvalidToken() {
        final var subject = tokenService.verifyToken("invalid.token.here");

        assertThat(subject).isEmpty();
    }

    @Test
    void shouldGenerateRefreshToken() {
        final var refreshToken = tokenService.generateRefreshToken();

        assertThat(refreshToken).isNotBlank();
        assertThat(refreshToken).hasSize(36); // UUID format
    }

    @Test
    void shouldExtractTokenFromBearerHeader() {
        final var token = tokenService.extractToken("Bearer my-jwt-token");

        assertThat(token).isEqualTo("my-jwt-token");
    }

    @Test
    void shouldThrowExceptionForInvalidAuthorizationHeader() {
        assertThatThrownBy(() -> tokenService.extractToken("InvalidHeader"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid authorization header");
    }

    @Test
    void shouldThrowExceptionForNullAuthorizationHeader() {
        assertThatThrownBy(() -> tokenService.extractToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid authorization header");
    }
}
