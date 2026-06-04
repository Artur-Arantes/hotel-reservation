package br.com.hotel.reservation.service;

import br.com.hotel.reservation.domain.Hotel;
import br.com.hotel.reservation.domain.Reservation;
import br.com.hotel.reservation.domain.Room;
import br.com.hotel.reservation.dto.ReservationRequest;
import br.com.hotel.reservation.enums.ReservationStatus;
import br.com.hotel.reservation.enums.RoomType;
import br.com.hotel.reservation.exception.ResourceNotFoundException;
import br.com.hotel.reservation.exception.RoomNotAvailableException;
import br.com.hotel.reservation.repository.ReservationRepository;
import br.com.hotel.reservation.repository.RoomRepository;
import br.com.hotel.reservation.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Hotel hotel;
    private Room room;
    private Reservation reservation;
    private ReservationRequest request;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder()
                .id(1L)
                .name("Hotel Atlântico")
                .city("Rio de Janeiro")
                .state("RJ")
                .build();

        room = Room.builder()
                .id(1L)
                .hotel(hotel)
                .roomNumber("101")
                .type(RoomType.STANDARD)
                .capacity(2)
                .price(new BigDecimal("350.00"))
                .build();

        reservation = Reservation.builder()
                .id(1L)
                .room(room)
                .guestName("Artur")
                .guestEmail("artur@email.com")
                .checkIn(LocalDate.of(2026, 7, 10))
                .checkOut(LocalDate.of(2026, 7, 15))
                .totalPrice(new BigDecimal("1750.00"))
                .status(ReservationStatus.PENDING)
                .build();

        request = new ReservationRequest(
                1L,
                "Artur",
                "artur@email.com",
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 15)
        );
    }

    @Test
    void shouldReturnAllReservations() {
        final var pageable = PageRequest.of(0, 10);
        when(reservationRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(reservation)));

        final var result = reservationService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).guestName()).isEqualTo("Artur");
    }

    @Test
    void shouldReturnReservationById() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        final var result = reservationService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.guestEmail()).isEqualTo("artur@email.com");
    }

    @Test
    void shouldThrowExceptionWhenReservationNotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldCreateReservationSuccessfully() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.findAvailableRooms(any(), any(), any())).thenReturn(List.of(room));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        final var result = reservationService.create(request);

        assertThat(result.guestName()).isEqualTo("Artur");
        assertThat(result.status()).isEqualTo(ReservationStatus.PENDING);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void shouldThrowExceptionWhenRoomNotAvailable() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.findAvailableRooms(any(), any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(RoomNotAvailableException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCheckOutBeforeCheckIn() {
        final var invalidRequest = new ReservationRequest(
                1L, "Artur", "artur@email.com",
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 7, 10)
        );

        assertThatThrownBy(() -> reservationService.create(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Check-out must be after check-in");
    }

    @Test
    void shouldCancelReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        final var result = reservationService.cancel(1L);

        verify(reservationRepository).save(any(Reservation.class));
        assertThat(result).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenCancellingAlreadyCancelledReservation() {
        reservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancel(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already cancelled");
    }
}
