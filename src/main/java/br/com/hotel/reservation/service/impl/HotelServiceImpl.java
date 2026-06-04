package br.com.hotel.reservation.service.impl;

import br.com.hotel.reservation.domain.Hotel;
import br.com.hotel.reservation.dto.HotelRequest;
import br.com.hotel.reservation.dto.HotelResponse;
import br.com.hotel.reservation.exception.ResourceNotFoundException;
import br.com.hotel.reservation.repository.HotelRepository;
import br.com.hotel.reservation.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponse> findAll(final Pageable pageable) {
        return hotelRepository.findAll(pageable).map(HotelResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponse findById(final Long id) {
        return hotelRepository.findById(id)
                .map(HotelResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
    }

    @Override
    @Transactional
    public HotelResponse create(final HotelRequest request) {
        final var hotel = Hotel.builder()
                .name(request.name())
                .city(request.city())
                .state(request.state())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .build();

        return HotelResponse.from(hotelRepository.save(hotel));
    }

    @Override
    @Transactional
    public HotelResponse update(final Long id, final HotelRequest request) {
        final var hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

        hotel.setName(request.name());
        hotel.setCity(request.city());
        hotel.setState(request.state());
        hotel.setDescription(request.description());
        hotel.setImageUrl(request.imageUrl());

        return HotelResponse.from(hotelRepository.save(hotel));
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hotel not found with id: " + id);
        }
        hotelRepository.deleteById(id);
    }
}
