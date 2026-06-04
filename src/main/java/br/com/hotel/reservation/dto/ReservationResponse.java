package br.com.hotel.reservation.dto;

import br.com.hotel.reservation.domain.Reservation;
import br.com.hotel.reservation.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public record ReservationResponse(
        Long id,
        Long roomId,
        String roomNumber,
        Long hotelId,
        String hotelName,
        String guestName,
        String guestEmail,
        LocalDate checkIn,
        LocalDate checkOut,
        BigDecimal totalPrice,
        ReservationStatus status,
        ZonedDateTime createdAt
) {
    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getRoom().getId(),
                reservation.getRoom().getRoomNumber(),
                reservation.getRoom().getHotel().getId(),
                reservation.getRoom().getHotel().getName(),
                reservation.getGuestName(),
                reservation.getGuestEmail(),
                reservation.getCheckIn(),
                reservation.getCheckOut(),
                reservation.getTotalPrice(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}
