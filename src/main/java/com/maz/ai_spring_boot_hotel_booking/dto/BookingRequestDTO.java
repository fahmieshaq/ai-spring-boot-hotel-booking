package com.maz.ai_spring_boot_hotel_booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request DTO for creating a Booking.
 *
 * Example JSON body for POST /bookings:
 * {
 *   "guestName": "John Smith",
 *   "checkInDate": "2026-10-01",
 *   "checkOutDate": "2026-10-05",
 *   "roomId": 1
 * }
 *
 * Note: "check-out must be after check-in" is a cross-field rule that
 * single-field annotations can't express - the service layer enforces it.
 */
public record BookingRequestDTO(
        @NotBlank(message = "Guest name is required")
        String guestName,

        // Today or later - you can't check in on a past date
        @NotNull(message = "Check-in date is required")
        @FutureOrPresent(message = "Check-in date must be today or in the future")
        LocalDate checkInDate,

        // Strictly after today
        @NotNull(message = "Check-out date is required")
        @Future(message = "Check-out date must be in the future")
        LocalDate checkOutDate,

        @NotNull(message = "Room id is required")
        Long roomId
) {
}
