package com.maz.ai_spring_boot_hotel_booking.service;

import com.maz.ai_spring_boot_hotel_booking.dto.HotelRequestDTO;
import com.maz.ai_spring_boot_hotel_booking.dto.HotelResponseDTO;
import com.maz.ai_spring_boot_hotel_booking.entity.Hotel;
import com.maz.ai_spring_boot_hotel_booking.exception.ResourceNotFoundException;
import com.maz.ai_spring_boot_hotel_booking.repository.HotelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Hotel.
 *
 * Responsibilities:
 * - Business logic (none complex for Hotel, but this is where it would live)
 * - Uses the repository layer for persistence
 * - Maps between entities (internal model) and DTOs (API model)
 * - Controllers never see entities; repositories never see DTOs
 *
 * @Service registers this class as a Spring bean so it can be
 * constructor-injected into controllers.
 *
 * @Transactional(readOnly = true) at class level: every method runs in a
 * read-only transaction by default (allows DB optimizations). Write methods
 * override it with plain @Transactional.
 */
@Service
@Transactional(readOnly = true) // The rule: class-level = default for all methods;
public class HotelService {

    /**
     * SLF4J logger. 'static final' = one logger instance per class.
     * SLF4J is a logging facade - the actual implementation (Logback,
     * included by Spring Boot) is pluggable without code changes.
     */
    private static final Logger log = LoggerFactory.getLogger(HotelService.class);

    /**
     * 'final' + constructor injection (below) = the dependency is mandatory
     * and immutable. Preferred over field @Autowired because:
     * - the bean can never exist in a half-initialized state
     * - easy to unit test: new HotelService(mockRepository)
     * - circular dependencies fail fast at startup
     */
    private final HotelRepository hotelRepository;

    /**
     * Constructor injection. Since Spring 4.3, a single constructor needs
     * no @Autowired annotation - Spring injects automatically.
     */
    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    /**
     * Creates a hotel from the request DTO and returns the saved state
     * (including the database-generated id).
     */
    @Transactional // OVERRIDE: read-write transaction.
    // @Transactional on a write method means "everything in this method succeeds together or rolls back together."
    public HotelResponseDTO createHotel(HotelRequestDTO request) {
        log.info("Creating hotel with name={} location={}", request.name(), request.location());

        Hotel hotel = new Hotel(request.name(), request.location());
        Hotel saved = hotelRepository.save(hotel);

        log.info("Hotel created with id={}", saved.getId());
        return toResponse(saved);
    }

    // no annotation → INHERITS class default (readOnly = true)
    public List<HotelResponseDTO> getAllHotels() {
        log.info("Fetching all hotels");

        // This is like a loop for each Hotel object, we map it to HotelResponseDTO
        return hotelRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * findById returns Optional<Hotel>; orElseThrow unwraps it or throws.
     * The global exception handler converts the exception to HTTP 404.
     */
    public HotelResponseDTO getHotelById(Long id) {
        log.info("Fetching hotel with id={}", id);

        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
        return toResponse(hotel);
    }

    /**
     * Update pattern: load the managed entity, mutate it, save.
     * Never trust an id from the request body - the path id rules.
     */
    @Transactional
    public HotelResponseDTO updateHotel(Long id, HotelRequestDTO request) {
        log.info("Updating hotel with id={}", id);

        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }

        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));

        hotel.setName(request.name());
        hotel.setLocation(request.location());
        Hotel updated = hotelRepository.save(hotel);

        log.info("Hotel updated with id={}", updated.getId());
        return toResponse(updated);
    }

    /**
     * Check existence first so the client gets a clean 404 instead of
     * a silent no-op or a raw database error.
     */
    @Transactional
    public void deleteHotel(Long id) {
        log.info("Deleting hotel with id={}", id);

        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }

        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hotel not found with id: " + id);
        }

        hotelRepository.deleteById(id);
        log.info("Hotel deleted with id={}", id);
    }

    /**
     * Entity -> DTO mapping, kept private: mapping is an internal concern
     * of the service. (In larger projects this moves to a dedicated mapper
     * class, e.g. MapStruct.)
     */
    private HotelResponseDTO toResponse(Hotel hotel) {
        return new HotelResponseDTO(hotel.getId(), hotel.getName(), hotel.getLocation());
    }
}
