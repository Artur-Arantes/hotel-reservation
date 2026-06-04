package br.com.hotel.reservation.service;

import br.com.hotel.reservation.domain.Hotel;
import br.com.hotel.reservation.dto.HotelRequest;
import br.com.hotel.reservation.exception.ResourceNotFoundException;
import br.com.hotel.reservation.repository.HotelRepository;
import br.com.hotel.reservation.service.impl.HotelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private Hotel hotel;
    private HotelRequest hotelRequest;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder()
                .id(1L)
                .name("Hotel Atlântico")
                .city("Rio de Janeiro")
                .state("RJ")
                .description("Hotel na orla")
                .imageUrl(null)
                .build();

        hotelRequest = new HotelRequest("Hotel Atlântico", "Rio de Janeiro", "RJ", "Hotel na orla", null);
    }

    @Test
    void shouldReturnAllHotels() {
        final var pageable = PageRequest.of(0, 10);
        when(hotelRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(hotel)));

        final var result = hotelService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Hotel Atlântico");
        verify(hotelRepository).findAll(pageable);
    }

    @Test
    void shouldReturnHotelById() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        final var result = hotelService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.city()).isEqualTo("Rio de Janeiro");
    }

    @Test
    void shouldThrowExceptionWhenHotelNotFound() {
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldCreateHotel() {
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        final var result = hotelService.create(hotelRequest);

        assertThat(result.name()).isEqualTo("Hotel Atlântico");
        verify(hotelRepository).save(any(Hotel.class));
    }

    @Test
    void shouldDeleteHotel() {
        when(hotelRepository.existsById(1L)).thenReturn(true);

        hotelService.delete(1L);

        verify(hotelRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentHotel() {
        when(hotelRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> hotelService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(hotelRepository, never()).deleteById(any());
    }
}
