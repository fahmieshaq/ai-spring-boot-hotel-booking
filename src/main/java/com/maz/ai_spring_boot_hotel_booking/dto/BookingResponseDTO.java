package com.maz.ai_spring_boot_hotel_booking.dto;

import java.time.LocalDate;

/**
 * Response DTO for Booking.
 *
 * Flattens room info (roomId + roomNumber) so the client gets
 * useful details in one response without nested objects.
 */
public record BookingResponseDTO(
        Long id,
        String guestName,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Long roomId,
        String roomNumber
) {
}
