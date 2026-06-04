package br.com.hotel.reservation.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}
