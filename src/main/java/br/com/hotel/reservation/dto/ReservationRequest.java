package br.com.hotel.reservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationRequest(
        @NotNull Long roomId,
        @NotBlank String guestName,
        @NotBlank @Email String guestEmail,
        @NotNull @Future LocalDate checkIn,
        @NotNull @Future LocalDate checkOut
) {}
