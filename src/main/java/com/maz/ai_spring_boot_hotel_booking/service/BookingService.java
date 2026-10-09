package com.maz.ai_spring_boot_hotel_booking.service;

import com.maz.ai_spring_boot_hotel_booking.dto.BookingRequestDTO;
import com.maz.ai_spring_boot_hotel_booking.dto.BookingResponseDTO;
import com.maz.ai_spring_boot_hotel_booking.entity.Booking;
import com.maz.ai_spring_boot_hotel_booking.entity.Room;
import com.maz.ai_spring_boot_hotel_booking.exception.BusinessRuleException;
import com.maz.ai_spring_boot_hotel_booking.exception.ResourceNotFoundException;
import com.maz.ai_spring_boot_hotel_booking.repository.BookingRepository;
import com.maz.ai_spring_boot_hotel_booking.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Booking.
 *
 * New concept vs RoomService: a cross-field business rule.
 * "check-out must be after check-in" involves TWO fields, so simple
 * field-level validation annotations can't express it - it belongs here,
 * in the service layer, where business rules live.
 */
@Service
@Transactional(readOnly = true)
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    /**
     * Creates a booking for an existing room.
     *
     * Order of checks matters:
     * 1. Business rule (dates) - cheapest check, no DB access needed
     * 2. Room existence - one DB read
     * 3. Insert
     */
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        log.info("Creating booking for guest={} roomId={} checkIn={} checkOut={}",
                request.guestName(), request.roomId(), request.checkInDate(), request.checkOutDate());

        // Cross-field business rule - not expressible with field annotations
        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            log.warn("Rejected booking for guest={}: check-out {} is not after check-in {}",
                    request.guestName(), request.checkOutDate(), request.checkInDate());
            // Custom exception, not IllegalArgumentException: our handler maps
            // by TYPE, and only our own code can throw BusinessRuleException,
            // so a 400 here can never be confused with a library bug (500).
            throw new BusinessRuleException("Check-out date must be after check-in date");
        }

        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found with id: " + request.roomId()));

        Booking booking = new Booking(
                request.guestName(),
                request.checkInDate(),
                request.checkOutDate(),
                room);
        Booking saved = bookingRepository.save(booking);

        log.info("Booking created with id={}", saved.getId());
        return toResponse(saved);
    }

    public List<BookingResponseDTO> getAllBookings() {
        log.info("Fetching all bookings");
        return bookingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Entity -> DTO mapping. Flattens the Room relationship into
     * roomId + roomNumber.
     */
    private BookingResponseDTO toResponse(Booking booking) {
        return new BookingResponseDTO(
                booking.getId(),
                booking.getGuestName(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getRoom().getId(),
                booking.getRoom().getRoomNumber());
    }
}
