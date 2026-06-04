package br.com.hotel.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HotelRequest(
        @NotBlank String name,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(min = 2, max = 2) String state,
        String description,
        String imageUrl
) {}
