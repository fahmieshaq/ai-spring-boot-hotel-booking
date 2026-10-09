package com.maz.ai_spring_boot_hotel_booking.service;

import com.maz.ai_spring_boot_hotel_booking.dto.RoomRequestDTO;
import com.maz.ai_spring_boot_hotel_booking.dto.RoomResponseDTO;
import com.maz.ai_spring_boot_hotel_booking.entity.Hotel;
import com.maz.ai_spring_boot_hotel_booking.entity.Room;
import com.maz.ai_spring_boot_hotel_booking.exception.ResourceNotFoundException;
import com.maz.ai_spring_boot_hotel_booking.repository.HotelRepository;
import com.maz.ai_spring_boot_hotel_booking.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Room.
 *
 * New concept vs HotelService: cross-entity logic.
 * The client sends only hotelId; this service resolves it to a real
 * Hotel entity (or 404s) before creating the Room. A service may use
 * multiple repositories - that's normal and expected.
 */
@Service
@Transactional(readOnly = true)
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    /**
     * Creates a room attached to an existing hotel.
     * Validates the referenced hotel exists - if not, 404 before any insert.
     */
    @Transactional
    public RoomResponseDTO createRoom(RoomRequestDTO request) {
        log.info("Creating room number={} for hotelId={}", request.roomNumber(), request.hotelId());

        Hotel hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel not found with id: " + request.hotelId()));

        Room room = new Room(request.roomNumber(), request.price(), hotel);
        Room saved = roomRepository.save(room);

        log.info("Room created with id={}", saved.getId());
        return toResponse(saved);
    }

    public List<RoomResponseDTO> getAllRooms() {
        log.info("Fetching all rooms");
        return roomRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RoomResponseDTO getRoomById(Long id) {
        log.info("Fetching room with id={}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        return toResponse(room);
    }

    /**
     * Entity -> DTO mapping. Flattens the Hotel relationship into
     * hotelId + hotelName so the JSON has no nested objects and
     * no recursion risk.
     */
    private RoomResponseDTO toResponse(Room room) {
        return new RoomResponseDTO(
                room.getId(),
                room.getRoomNumber(),
                room.getPrice(),
                room.getHotel().getId(),
                room.getHotel().getName());
    }
}
