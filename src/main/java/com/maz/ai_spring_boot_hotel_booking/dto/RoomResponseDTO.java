package com.maz.ai_spring_boot_hotel_booking.dto;

import java.math.BigDecimal;

/**
 * Response DTO for Room.
 *
 * Flattens the relationship: hotelId + hotelName instead of a nested
 * Hotel object. The client gets the info it needs in one response,
 * with no JSON recursion risk.
 */
public record RoomResponseDTO(
        Long id,
        String roomNumber,
        BigDecimal price,
        Long hotelId,
        String hotelName
) {
}
