package com.maz.ai_spring_boot_hotel_booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request DTO for creating a Room.
 *
 * The client sends hotelId (a plain number), NOT a nested Hotel object:
 *   { "roomNumber": "101", "price": 250.00, "hotelId": 1 }
 * The service layer looks up the real Hotel entity by this id.
 */
public record RoomRequestDTO(

        @NotBlank(message = "Room number is required")
        String roomNumber,

        // @NotNull for non-String types (@NotBlank only works on Strings);
        // @Positive means > 0 - a room can't be free or negative
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than zero")
        BigDecimal price,

        // The service layer validates this hotel actually exists
        @NotNull(message = "Hotel id is required")
        Long hotelId
) {
}
