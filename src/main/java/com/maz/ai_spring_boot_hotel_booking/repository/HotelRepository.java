package com.maz.ai_spring_boot_hotel_booking.repository;

import com.maz.ai_spring_boot_hotel_booking.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Hotel entity.
 *
 * JpaRepository<Hotel, Long> means:
 * - Hotel = the entity this repository manages
 * - Long  = the type of the entity's primary key (Hotel.id)
 *
 * We only declare the interface. Spring Data JPA generates the
 * implementation automatically at runtime (a proxy class).
 *
 * Inherited methods we get for free (no code needed):
 * - save(hotel)        -> INSERT or UPDATE (decides based on whether id exists)
 * - findById(id)       -> SELECT by primary key, returns Optional<Hotel>
 * - findAll()          -> SELECT all rows
 * - deleteById(id)     -> DELETE by primary key
 * - existsById(id)     -> checks existence
 * - count()            -> row count
 *
 * @Repository is technically optional here (Spring detects JpaRepository
 * interfaces automatically), but it documents intent and enables
 * exception translation semantics explicitly.
 */
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
