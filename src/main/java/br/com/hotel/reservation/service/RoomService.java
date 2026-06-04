package br.com.hotel.reservation.service;

import br.com.hotel.reservation.dto.AvailabilityRequest;
import br.com.hotel.reservation.dto.RoomRequest;
import br.com.hotel.reservation.dto.RoomResponse;

import java.util.List;

public interface RoomService {

    List<RoomResponse> findAvailableRooms(final AvailabilityRequest request);

    RoomResponse findById(final Long id);

    RoomResponse create(final RoomRequest request);

    void delete(final Long id);
}
