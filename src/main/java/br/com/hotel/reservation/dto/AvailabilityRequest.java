package br.com.hotel.reservation.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AvailabilityRequest(
        @NotNull Long hotelId,
        @NotNull @Future LocalDate checkIn,
        @NotNull @Future LocalDate checkOut
) {}
