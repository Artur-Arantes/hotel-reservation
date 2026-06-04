package br.com.hotel.reservation.service.impl;

import br.com.hotel.reservation.domain.Reservation;
import br.com.hotel.reservation.dto.ReservationRequest;
import br.com.hotel.reservation.dto.ReservationResponse;
import br.com.hotel.reservation.enums.ReservationStatus;
import br.com.hotel.reservation.exception.ResourceNotFoundException;
import br.com.hotel.reservation.exception.RoomNotAvailableException;
import br.com.hotel.reservation.repository.ReservationRepository;
import br.com.hotel.reservation.repository.RoomRepository;
import br.com.hotel.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> findAll(final Pageable pageable) {
        log.debug("Fetching reservations - page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return reservationRepository.findAll(pageable).map(ReservationResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> findByGuestEmail(final String email) {
        log.debug("Fetching reservations for email='{}'", email);
        return reservationRepository.findByGuestEmail(email)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse findById(final Long id) {
        log.debug("Fetching reservation id={}", id);
        return reservationRepository.findById(id)
                .map(ReservationResponse::from)
                .orElseThrow(() -> {
                    log.warn("Reservation not found id={}", id);
                    return new ResourceNotFoundException("Reservation not found with id: " + id);
                });
    }

    @Override
    @Transactional
    public ReservationResponse create(final ReservationRequest request) {
        log.info("Creating reservation roomId={} guest='{}' checkIn={} checkOut={}",
                request.roomId(), request.guestName(), request.checkIn(), request.checkOut());

        if (!request.checkOut().isAfter(request.checkIn())) {
            log.warn("Invalid dates: checkIn={} checkOut={}", request.checkIn(), request.checkOut());
            throw new IllegalArgumentException("Check-out must be after check-in");
        }

        final var room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> {
                    log.warn("Room not found id={}", request.roomId());
                    return new ResourceNotFoundException("Room not found with id: " + request.roomId());
                });

        final var available = roomRepository
                .findAvailableRooms(room.getHotel().getId(), request.checkIn(), request.checkOut())
                .stream()
                .anyMatch(r -> r.getId().equals(room.getId()));

        if (!available) {
            log.warn("Room {} not available from {} to {}", room.getRoomNumber(), request.checkIn(), request.checkOut());
            throw new RoomNotAvailableException("Room " + room.getRoomNumber() + " is not available for the selected period");
        }

        final var nights = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        final var totalPrice = room.getPrice().multiply(BigDecimal.valueOf(nights));

        final var reservation = Reservation.builder()
                .room(room)
                .guestName(request.guestName())
                .guestEmail(request.guestEmail())
                .checkIn(request.checkIn())
                .checkOut(request.checkOut())
                .totalPrice(totalPrice)
                .status(ReservationStatus.PENDING)
                .build();

        final var saved = reservationRepository.save(reservation);
        log.info("Reservation created id={} room={} guest='{}' total={}",
                saved.getId(), room.getRoomNumber(), request.guestName(), totalPrice);
        return ReservationResponse.from(saved);
    }

    @Override
    @Transactional
    public ReservationResponse cancel(final Long id) {
        log.info("Cancelling reservation id={}", id);
        final var reservation = reservationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Reservation not found for cancellation id={}", id);
                    return new ResourceNotFoundException("Reservation not found with id: " + id);
                });

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            log.warn("Reservation id={} is already cancelled", id);
            throw new IllegalStateException("Reservation is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        log.info("Reservation cancelled id={}", id);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }
}
