package com.maz.ai_spring_boot_hotel_booking.controller;

import com.maz.ai_spring_boot_hotel_booking.dto.RoomRequestDTO;
import com.maz.ai_spring_boot_hotel_booking.dto.RoomResponseDTO;
import com.maz.ai_spring_boot_hotel_booking.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Room endpoints.
 */
@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * POST /rooms
     *
     * Body carries hotelId; if that hotel doesn't exist the service throws
     * ResourceNotFoundException -> 404. If validation fails (@Valid) ->
     * 400 before the service is even called.
     */
    @PostMapping
    public ResponseEntity<RoomResponseDTO> createRoom(@Valid @RequestBody RoomRequestDTO request) {
        RoomResponseDTO response = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /rooms
     */
    @GetMapping
    public List<RoomResponseDTO> getAllRooms() {
        return roomService.getAllRooms();
    }

    /**
     * GET /rooms/{id}
     */
    @GetMapping("/{id}")
    public RoomResponseDTO getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id);
    }
}
