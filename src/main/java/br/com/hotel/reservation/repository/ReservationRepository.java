package br.com.hotel.reservation.repository;

import br.com.hotel.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByGuestEmail(String guestEmail);
}
