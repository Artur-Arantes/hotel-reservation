package br.com.hotel.reservation.service;

import br.com.hotel.reservation.domain.Hotel;
import br.com.hotel.reservation.domain.Room;
import br.com.hotel.reservation.dto.AvailabilityRequest;
import br.com.hotel.reservation.dto.RoomRequest;
import br.com.hotel.reservation.enums.RoomType;
import br.com.hotel.reservation.exception.ResourceNotFoundException;
import br.com.hotel.reservation.repository.HotelRepository;
import br.com.hotel.reservation.repository.RoomRepository;
import br.com.hotel.reservation.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Hotel hotel;
    private Room room;

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
    }

    @Test
    void shouldReturnAvailableRooms() {
        final var request = new AvailabilityRequest(1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        when(roomRepository.findAvailableRooms(1L, request.checkIn(), request.checkOut()))
                .thenReturn(List.of(room));

        final var result = roomService.findAvailableRooms(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).roomNumber()).isEqualTo("101");
    }

    @Test
    void shouldThrowExceptionWhenCheckOutBeforeCheckInOnAvailability() {
        final var request = new AvailabilityRequest(1L,
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> roomService.findAvailableRooms(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Check-out must be after check-in");
    }

    @Test
    void shouldReturnRoomById() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        final var result = roomService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.roomNumber()).isEqualTo("101");
    }

    @Test
    void shouldThrowExceptionWhenRoomNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldCreateRoomSuccessfully() {
        final var request = new RoomRequest(1L, "101", RoomType.STANDARD, 2, new BigDecimal("350.00"));
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        final var result = roomService.create(request);

        assertThat(result.roomNumber()).isEqualTo("101");
        assertThat(result.type()).isEqualTo(RoomType.STANDARD);
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void shouldThrowExceptionWhenHotelNotFoundOnCreate() {
        final var request = new RoomRequest(99L, "101", RoomType.STANDARD, 2, new BigDecimal("350.00"));
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(roomRepository, never()).save(any());
    }

    @Test
    void shouldDeleteRoomSuccessfully() {
        when(roomRepository.existsById(1L)).thenReturn(true);

        roomService.delete(1L);

        verify(roomRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentRoom() {
        when(roomRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> roomService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(roomRepository, never()).deleteById(any());
    }
}
