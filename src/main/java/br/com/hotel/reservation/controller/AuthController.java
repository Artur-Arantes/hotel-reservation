package br.com.hotel.reservation.controller;

import br.com.hotel.reservation.domain.User;
import br.com.hotel.reservation.dto.LoginRequest;
import br.com.hotel.reservation.dto.RefreshRequest;
import br.com.hotel.reservation.dto.RegisterRequest;
import br.com.hotel.reservation.dto.TokenResponse;
import br.com.hotel.reservation.exception.ResourceNotFoundException;
import br.com.hotel.reservation.repository.UserRepository;
import br.com.hotel.reservation.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Tag(name = "Auth", description = "Authentication and token management")
public class AuthController {

    private static final int REFRESH_TOKEN_EXPIRY_DAYS = 7;

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<TokenResponse> register(@RequestBody @Valid final RegisterRequest request) {
        final var user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .refreshToken(tokenService.generateRefreshToken())
                .refreshTokenExpiresAt(ZonedDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS))
                .build();

        userRepository.save(user);

        final var accessToken = tokenService.generateAccessToken(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TokenResponse(accessToken, user.getRefreshToken()));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and get tokens")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid final LoginRequest request) {
        final var usernamePassword = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        final var auth = authenticationManager.authenticate(usernamePassword);
        final var user = (User) auth.getPrincipal();

        final var refreshToken = tokenService.generateRefreshToken();
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiresAt(ZonedDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS));
        userRepository.save(user);

        final var accessToken = tokenService.generateAccessToken(user);
        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    @ApiResponse(responseCode = "200", description = "Token refreshed")
    @ApiResponse(responseCode = "404", description = "Invalid refresh token")
    @ApiResponse(responseCode = "400", description = "Refresh token expired")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid final RefreshRequest request) {
        final var user = userRepository.findByRefreshToken(request.refreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid refresh token"));

        if (user.getRefreshTokenExpiresAt() == null || user.getRefreshTokenExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new IllegalStateException("Refresh token has expired");
        }

        final var newRefreshToken = tokenService.generateRefreshToken();
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiresAt(ZonedDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS));
        userRepository.save(user);

        final var accessToken = tokenService.generateAccessToken(user);
        return ResponseEntity.ok(new TokenResponse(accessToken, newRefreshToken));
    }
}
