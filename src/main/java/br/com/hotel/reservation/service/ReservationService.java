package br.com.hotel.reservation.service;

import br.com.hotel.reservation.dto.ReservationRequest;
import br.com.hotel.reservation.dto.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReservationService {

    Page<ReservationResponse> findAll(final Pageable pageable);

    List<ReservationResponse> findByGuestEmail(final String email);

    ReservationResponse findById(final Long id);

    ReservationResponse create(final ReservationRequest request);

    ReservationResponse cancel(final Long id);
}
