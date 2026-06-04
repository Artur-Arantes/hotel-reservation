package br.com.hotel.reservation.repository;

import br.com.hotel.reservation.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("""
        SELECT r FROM Room r
        WHERE r.hotel.id = :hotelId
        AND NOT EXISTS (
            SELECT 1 FROM Reservation res
            WHERE res.room = r
            AND res.status IN ('PENDING', 'CONFIRMED')
            AND res.checkIn < :checkOut
            AND res.checkOut > :checkIn
        )
    """)
    List<Room> findAvailableRooms(
            @Param("hotelId") Long hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}
