package br.com.hotel.reservation.repository;

import br.com.hotel.reservation.domain.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
