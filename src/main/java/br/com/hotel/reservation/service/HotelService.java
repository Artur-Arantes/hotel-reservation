package br.com.hotel.reservation.service;

import br.com.hotel.reservation.dto.HotelRequest;
import br.com.hotel.reservation.dto.HotelResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HotelService {

    Page<HotelResponse> findAll(final Pageable pageable);

    HotelResponse findById(final Long id);

    HotelResponse create(final HotelRequest request);

    HotelResponse update(final Long id, final HotelRequest request);

    void delete(final Long id);
}
