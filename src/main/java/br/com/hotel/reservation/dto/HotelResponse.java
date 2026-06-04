package br.com.hotel.reservation.dto;

import br.com.hotel.reservation.domain.Hotel;

public record HotelResponse(
        Long id,
        String name,
        String city,
        String state,
        String description,
        String imageUrl
) {
    public static HotelResponse from(Hotel hotel) {
        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getCity(),
                hotel.getState(),
                hotel.getDescription(),
                hotel.getImageUrl()
        );
    }
}
