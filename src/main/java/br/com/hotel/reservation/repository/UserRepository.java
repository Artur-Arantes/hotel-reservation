package br.com.hotel.reservation.repository;

import br.com.hotel.reservation.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(final String email);

    Optional<User> findByRefreshToken(final String refreshToken);
}
