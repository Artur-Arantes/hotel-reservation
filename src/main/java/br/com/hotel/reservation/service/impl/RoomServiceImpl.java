package br.com.hotel.reservation.service.impl;

import br.com.hotel.reservation.domain.Room;
import br.com.hotel.reservation.dto.AvailabilityRequest;
import br.com.hotel.reservation.dto.RoomRequest;
import br.com.hotel.reservation.dto.RoomResponse;
import br.com.hotel.reservation.exception.ResourceNotFoundException;
import br.com.hotel.reservation.repository.HotelRepository;
import br.com.hotel.reservation.repository.RoomRepository;
import br.com.hotel.reservation.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> findAvailableRooms(final AvailabilityRequest request) {
        if (!request.checkOut().isAfter(request.checkIn())) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }
        return roomRepository.findAvailableRooms(request.hotelId(), request.checkIn(), request.checkOut())
                .stream()
                .map(RoomResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse findById(final Long id) {
        return roomRepository.findById(id)
                .map(RoomResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }

    @Override
    @Transactional
    public RoomResponse create(final RoomRequest request) {
        final var hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + request.hotelId()));

        final var room = Room.builder()
                .hotel(hotel)
                .roomNumber(request.roomNumber())
                .type(request.type())
                .capacity(request.capacity())
                .price(request.price())
                .build();

        return RoomResponse.from(roomRepository.save(room));
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }
}
