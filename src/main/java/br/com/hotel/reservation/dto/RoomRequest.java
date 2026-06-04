package br.com.hotel.reservation.dto;

import br.com.hotel.reservation.enums.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RoomRequest(
        @NotNull Long hotelId,
        @NotBlank String roomNumber,
        @NotNull RoomType type,
        @NotNull @Positive Integer capacity,
        @NotNull @Positive BigDecimal price
) {}
