package br.com.hotel.reservation.dto;

import br.com.hotel.reservation.domain.Room;
import br.com.hotel.reservation.enums.RoomType;

import java.math.BigDecimal;

public record RoomResponse(
        Long id,
        Long hotelId,
        String hotelName,
        String roomNumber,
        RoomType type,
        Integer capacity,
        BigDecimal price
) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getHotel().getId(),
                room.getHotel().getName(),
                room.getRoomNumber(),
                room.getType(),
                room.getCapacity(),
                room.getPrice()
        );
    }
}
