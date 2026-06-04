package br.com.hotel.reservation.service.impl;

import br.com.hotel.reservation.domain.Hotel;
import br.com.hotel.reservation.dto.HotelRequest;
import br.com.hotel.reservation.dto.HotelResponse;
import br.com.hotel.reservation.exception.ResourceNotFoundException;
import br.com.hotel.reservation.repository.HotelRepository;
import br.com.hotel.reservation.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponse> findAll(final Pageable pageable) {
        log.debug("Fetching hotels - page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return hotelRepository.findAll(pageable).map(HotelResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponse findById(final Long id) {
        log.debug("Fetching hotel id={}", id);
        return hotelRepository.findById(id)
                .map(HotelResponse::from)
                .orElseThrow(() -> {
                    log.warn("Hotel not found id={}", id);
                    return new ResourceNotFoundException("Hotel not found with id: " + id);
                });
    }

    @Override
    @Transactional
    public HotelResponse create(final HotelRequest request) {
        log.info("Creating hotel name='{}'", request.name());
        final var hotel = Hotel.builder()
                .name(request.name())
                .city(request.city())
                .state(request.state())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .build();

        final var saved = hotelRepository.save(hotel);
        log.info("Hotel created id={} name='{}'", saved.getId(), saved.getName());
        return HotelResponse.from(saved);
    }

    @Override
    @Transactional
    public HotelResponse update(final Long id, final HotelRequest request) {
        log.info("Updating hotel id={}", id);
        final var hotel = hotelRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Hotel not found for update id={}", id);
                    return new ResourceNotFoundException("Hotel not found with id: " + id);
                });

        hotel.setName(request.name());
        hotel.setCity(request.city());
        hotel.setState(request.state());
        hotel.setDescription(request.description());
        hotel.setImageUrl(request.imageUrl());

        log.info("Hotel updated id={}", id);
        return HotelResponse.from(hotelRepository.save(hotel));
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        log.info("Deleting hotel id={}", id);
        if (!hotelRepository.existsById(id)) {
            log.warn("Hotel not found for deletion id={}", id);
            throw new ResourceNotFoundException("Hotel not found with id: " + id);
        }
        hotelRepository.deleteById(id);
        log.info("Hotel deleted id={}", id);
    }
}
