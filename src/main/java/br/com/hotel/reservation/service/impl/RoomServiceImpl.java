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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> findAvailableRooms(final AvailabilityRequest request) {
        log.debug("Checking availability hotelId={} checkIn={} checkOut={}",
                request.hotelId(), request.checkIn(), request.checkOut());
        if (!request.checkOut().isAfter(request.checkIn())) {
            log.warn("Invalid dates for availability check: checkIn={} checkOut={}", request.checkIn(), request.checkOut());
            throw new IllegalArgumentException("Check-out must be after check-in");
        }
        final var rooms = roomRepository.findAvailableRooms(request.hotelId(), request.checkIn(), request.checkOut())
                .stream()
                .map(RoomResponse::from)
                .toList();
        log.debug("Found {} available rooms for hotelId={}", rooms.size(), request.hotelId());
        return rooms;
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse findById(final Long id) {
        log.debug("Fetching room id={}", id);
        return roomRepository.findById(id)
                .map(RoomResponse::from)
                .orElseThrow(() -> {
                    log.warn("Room not found id={}", id);
                    return new ResourceNotFoundException("Room not found with id: " + id);
                });
    }

    @Override
    @Transactional
    public RoomResponse create(final RoomRequest request) {
        log.info("Creating room number='{}' hotelId={}", request.roomNumber(), request.hotelId());
        final var hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> {
                    log.warn("Hotel not found for room creation id={}", request.hotelId());
                    return new ResourceNotFoundException("Hotel not found with id: " + request.hotelId());
                });

        final var room = Room.builder()
                .hotel(hotel)
                .roomNumber(request.roomNumber())
                .type(request.type())
                .capacity(request.capacity())
                .price(request.price())
                .build();

        final var saved = roomRepository.save(room);
        log.info("Room created id={} number='{}' hotelId={}", saved.getId(), saved.getRoomNumber(), hotel.getId());
        return RoomResponse.from(saved);
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        log.info("Deleting room id={}", id);
        if (!roomRepository.existsById(id)) {
            log.warn("Room not found for deletion id={}", id);
            throw new ResourceNotFoundException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
        log.info("Room deleted id={}", id);
    }
}
