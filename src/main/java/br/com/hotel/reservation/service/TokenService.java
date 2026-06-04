package br.com.hotel.reservation.service;

import br.com.hotel.reservation.domain.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class TokenService {

    @Value("${secret.key}")
    private String secretKey;

    @SneakyThrows
    public String generateAccessToken(final User user) {
        final var algorithm = Algorithm.HMAC256(secretKey);
        try {
            return JWT.create()
                    .withIssuer("hotel-reservation")
                    .withSubject(user.getEmail())
                    .withExpiresAt(accessTokenExpiration())
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new BadCredentialsException("Error generating access token");
        }
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public String verifyToken(final String token) {
        try {
            final var algorithm = Algorithm.HMAC256(secretKey);
            return JWT.require(algorithm)
                    .withIssuer("hotel-reservation")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            return "";
        }
    }

    public String extractToken(final String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.replace("Bearer ", "").trim();
        }
        throw new IllegalArgumentException("Invalid authorization header");
    }

    private Instant accessTokenExpiration() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
