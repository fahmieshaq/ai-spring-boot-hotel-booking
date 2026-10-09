package com.maz.ai_spring_boot_hotel_booking.repository;

import com.maz.ai_spring_boot_hotel_booking.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Room entity.
 *
 * JpaRepository<Room, Long>:
 * - Room = managed entity
 * - Long = primary key type
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Derived query method (query built from the method name).
     *
     * Spring Data JPA parses "findByHotelId" as:
     * - findBy  -> SELECT
     * - Hotel   -> the 'hotel' field on Room
     * - Id      -> the 'id' field inside Hotel
     *
     * Generated SQL is roughly:
     * SELECT * FROM room WHERE hotel_id = ?
     *
     * No implementation and no SQL needed - the method name IS the query.
     */
    List<Room> findByHotelId(Long hotelId);
}
