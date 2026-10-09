package com.maz.ai_spring_boot_hotel_booking.repository;

import com.maz.ai_spring_boot_hotel_booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Booking entity.
 *
 * JpaRepository<Booking, Long>:
 * - Booking = managed entity
 * - Long = primary key type
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Derived query: find all bookings for a given room.
     *
     * Method name parsing:
     * - findBy -> SELECT
     * - Room   -> the 'room' field on Booking
     * - Id     -> the 'id' field inside Room
     *
     * Generated SQL is roughly:
     * SELECT * FROM booking WHERE room_id = ?
     */
    List<Booking> findByRoomId(Long roomId);
}
