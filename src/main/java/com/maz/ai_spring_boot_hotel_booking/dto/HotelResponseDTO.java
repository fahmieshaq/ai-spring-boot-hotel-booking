package com.maz.ai_spring_boot_hotel_booking.dto;

/**
 * Response DTO for Hotel (what GET /hotels returns).
 *
 * - Includes id (client needs it to reference the hotel in later calls)
 * - No validation annotations: we validate input, not output
 * - No rooms list: avoids infinite JSON recursion (Hotel -> rooms -> hotel -> ...)
 *   and decouples the API shape from the database shape
 */
public record HotelResponseDTO(
        Long id,
        String name,
        String location
) {
}
