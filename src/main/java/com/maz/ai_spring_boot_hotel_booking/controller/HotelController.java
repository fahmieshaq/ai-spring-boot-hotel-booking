package com.maz.ai_spring_boot_hotel_booking.controller;

import com.maz.ai_spring_boot_hotel_booking.dto.HotelRequestDTO;
import com.maz.ai_spring_boot_hotel_booking.dto.HotelResponseDTO;
import com.maz.ai_spring_boot_hotel_booking.service.HotelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Hotel endpoints.
 *
 * @RestController = @Controller + @ResponseBody:
 * - registers this class as a Spring bean handling HTTP requests
 * - every return value is serialized to JSON automatically (via Jackson)
 *
 * @RequestMapping("/hotels") = base path; every method's path is
 * relative to it.
 *
 * Controllers are deliberately thin: translate HTTP <-> DTOs, delegate
 * to the service, choose the status code. No business logic here.
 */
@RestController
@RequestMapping("/hotels")
public class HotelController {

    /**
     * Same constructor-injection pattern as in the service:
     * Spring builds HotelService first, then passes it in here.
     */
    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    /**
     * POST /hotels
     *
     * @RequestBody = deserialize the JSON request body into HotelRequestDTO
     * @Valid       = THE trigger for the validation annotations on the DTO.
     *                Without it, @NotBlank etc. are silently ignored.
     *                If validation fails, Spring throws
     *                MethodArgumentNotValidException before this method runs
     *                (handled globally in Step 7 -> HTTP 400).
     *
     * ResponseEntity lets us control the HTTP status: REST convention for
     * a successful creation is 201 Created, not the default 200 OK.
     */
    @PostMapping
    public ResponseEntity<HotelResponseDTO> createHotel(@Valid @RequestBody HotelRequestDTO request) {
        HotelResponseDTO response = hotelService.createHotel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /hotels
     *
     * Returning the list directly (no ResponseEntity) is fine when the
     * status is always 200 OK - Spring wraps it automatically.
     */
    @GetMapping
    public List<HotelResponseDTO> getAllHotels() {
        return hotelService.getAllHotels();
    }

    /**
     * GET /hotels/{id}
     *
     * @PathVariable binds the {id} placeholder from the URL to the method
     * parameter (names must match, or use @PathVariable("id")).
     *
     * If the id doesn't exist, the service throws ResourceNotFoundException;
     * the global handler (Step 7) turns it into HTTP 404. Note this method
     * has no error handling code - that's the point.
     */
    @GetMapping("/{id}")
    public HotelResponseDTO getHotelById(@PathVariable Long id) {
        return hotelService.getHotelById(id);
    }

    /**
     * PUT /hotels/{id}
     *
     * PUT = full replacement of the resource. The id comes from the URL
     * path (authoritative), the new state from the validated body.
     */
    @PutMapping("/{id}")
    public HotelResponseDTO updateHotel(@PathVariable Long id,
                                        @Valid @RequestBody HotelRequestDTO request) {
        return hotelService.updateHotel(id, request);
    }

    /**
     * DELETE /hotels/{id}
     *
     * REST convention for successful delete: 204 No Content - the operation
     * succeeded and there is nothing to return.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build(); // REST conventions we followed: create → 201 Created, delete → 204 No Content.
    }

    //    @DeleteMapping("/{id}")
    //    public void deleteHotel(@PathVariable Long id) {
    //        hotelService.deleteHotel(id);
    //    }
    //    // → 200 OK with empty body ---> Wrong status code
}
