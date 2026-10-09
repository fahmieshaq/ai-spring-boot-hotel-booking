package com.maz.ai_spring_boot_hotel_booking.controller;

import com.maz.ai_spring_boot_hotel_booking.dto.BookingRequestDTO;
import com.maz.ai_spring_boot_hotel_booking.dto.BookingResponseDTO;
import com.maz.ai_spring_boot_hotel_booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Booking endpoints.
 */
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * POST /bookings
     *
     * Three ways this request can fail, each handled in a different layer:
     * - invalid fields (blank guest name, past dates) -> @Valid -> 400
     * - room id doesn't exist -> service throws ResourceNotFoundException -> 404
     * - check-out not after check-in -> service business rule
     */
    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO request) {
        BookingResponseDTO response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /bookings
     */
    @GetMapping
    public List<BookingResponseDTO> getAllBookings() {
        return bookingService.getAllBookings();
    }
}
