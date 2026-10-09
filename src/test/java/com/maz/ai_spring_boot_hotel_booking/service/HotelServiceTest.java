package com.maz.ai_spring_boot_hotel_booking.service;

import com.maz.ai_spring_boot_hotel_booking.dto.HotelRequestDTO;
import com.maz.ai_spring_boot_hotel_booking.dto.HotelResponseDTO;
import com.maz.ai_spring_boot_hotel_booking.entity.Hotel;
import com.maz.ai_spring_boot_hotel_booking.exception.ResourceNotFoundException;
import com.maz.ai_spring_boot_hotel_booking.repository.HotelRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/*
 * ============================================================================
 * JUNIT vs MOCKITO
 * ============================================================================
 *
 * JUNIT
 * -----
 * JUnit is the TESTING FRAMEWORK.
 *
 * Its job is to:
 * - Discover tests through annotations such as @Test.
 * - Execute the test methods.
 * - Determine whether tests pass or fail.
 *
 * Example:
 *
 *     @Test
 *     void createHotel_shouldReturnSavedHotelResponse() { ... }
 *
 * @Test comes from JUnit.
 *
 *
 * MOCKITO
 * -------
 * Mockito is a MOCKING FRAMEWORK.
 *
 * Its job is to create fake versions of dependencies used by the class
 * we are testing.
 *
 * In this test we want to test HotelService, NOT HotelRepository,
 * Hibernate, JPA, or PostgreSQL.
 *
 * Therefore:
 *
 *     HotelService     = REAL
 *     HotelRepository  = MOCK
 *
 * There is NO real database operation in this unit test.
 *
 * Mockito gives us:
 *
 *     @Mock
 *     @InjectMocks
 *     when(...)
 *     thenAnswer(...)
 *     verify(...)
 *     any(...)
 *
 *
 * ASSERTJ
 * -------
 * AssertJ provides the assertThat(...) syntax used to check the final result.
 *
 * Example:
 *
 *     assertThat(response.name()).isEqualTo("Grand Plaza");
 *
 *
 * In short:
 *
 *     JUnit   -> Runs the test.
 *     Mockito -> Fakes dependencies and verifies interactions with them.
 *     AssertJ -> Checks that actual results equal expected results.
 * ============================================================================
 */
@ExtendWith(MockitoExtension.class)
class HotelServiceTest {


    /*
     * @Mock is from Mockito.
     *
     * Mockito creates a FAKE HotelRepository for this test.
     *
     * This means:
     *
     *     hotelRepository.save(...)
     *
     * will NOT:
     *
     * - connect to PostgreSQL
     * - execute INSERT SQL
     * - use Hibernate to persist anything
     * - actually save anything
     *
     * We control how this fake repository behaves using when(...).
     */
    @Mock
    private HotelRepository hotelRepository;


    /*
     * @InjectMocks is from Mockito.
     *
     * HotelService is the REAL object that we want to test.
     *
     * Mockito creates HotelService and injects our mocked
     * HotelRepository into it.
     *
     * Conceptually, Mockito is doing something similar to:
     *
     *     HotelRepository mockRepository = mock(HotelRepository.class);
     *     HotelService hotelService = new HotelService(mockRepository);
     *
     * Therefore:
     *
     *     HotelService    -> REAL
     *     HotelRepository -> MOCK
     */
    @InjectMocks
    private HotelService hotelService;


    /*
     * @Test is from JUnit.
     *
     * It tells JUnit:
     *
     *     "This method is a test. Execute it when the test suite runs."
     */
    @Test
    void createHotel_returnsSavedHotelResponse_success() {

        // ====================================================================
        // GIVEN
        // ====================================================================
        //
        // GIVEN means:
        //
        //     Prepare the conditions/input required by the test
        //     BEFORE executing the method we want to test.


        /*
         * Create the request that will be passed to the REAL HotelService.
         *
         * This DTO is NOT mocked.
         *
         * It is a normal, real Java object.
         *
         * In the real application, the controller might receive:
         *
         *     {
         *         "name": "Grand Plaza",
         *         "location": "Muscat"
         *     }
         *
         * and Spring would deserialize that JSON into HotelRequestDTO.
         *
         * Because this is a SERVICE UNIT TEST, we are not testing the
         * controller or JSON deserialization. We simply construct the DTO.
         */
        HotelRequestDTO request =
                new HotelRequestDTO("Grand Plaza", "Muscat");


        /*
         * Configure how the MOCK HotelRepository should behave when
         * the REAL HotelService calls:
         *
         *     hotelRepository.save(hotel)
         *
         *
         * any(Hotel.class)
         * ----------------
         *
         * Means:
         *
         *     Match any Hotel object passed to save().
         *
         *
         * thenAnswer(...)
         * ---------------
         *
         * We use thenAnswer() here instead of creating a separate
         * "perfect" Hotel and returning it with thenReturn().
         *
         * WHY?
         *
         * We want the repository mock to return the ACTUAL Hotel that
         * HotelService attempted to save.
         *
         * Suppose HotelService has a bug and creates:
         *
         *     new Hotel("WRONG NAME", "WRONG LOCATION")
         *
         * We WANT those wrong values to flow through the test so our
         * response assertions fail.
         *
         * If we instead did:
         *
         *     when(hotelRepository.save(any(Hotel.class)))
         *         .thenReturn(
         *             new Hotel(1L, "Grand Plaza", "Muscat")
         *         );
         *
         * we could accidentally hide the bug because the mock would
         * return "Grand Plaza" and "Muscat" regardless of what the
         * service actually attempted to save.
         *
         *
         * invocation.getArgument(0)
         * -------------------------
         *
         * save(...) receives one argument:
         *
         *     save(hotel)
         *          ^
         *          argument #0
         *
         * getArgument(0) therefore retrieves the ACTUAL Hotel object
         * that HotelService passed to the mocked repository.
         *
         *
         * hotel.setId(1L)
         * ----------------
         *
         * Because there is no real database, PostgreSQL will not generate
         * an ID for us.
         *
         * We simulate ONLY that part of persistence by assigning ID 1.
         *
         *
         * return hotel
         * ------------
         *
         * Return the SAME Hotel that HotelService attempted to save.
         *
         * We are NOT manufacturing a new Hotel containing the expected
         * name/location.
         */
        when(hotelRepository.save(any(Hotel.class)))
                .thenAnswer(invocation -> {

                    // Get the actual Hotel passed by HotelService to save().
                    Hotel hotel = invocation.getArgument(0);

                    // Simulate the database generating a primary key.
                    hotel.setId(1L);

                    // Return the same Hotel back to HotelService.
                    return hotel;
                });


        // ====================================================================
        // WHEN
        // ====================================================================
        //
        // WHEN means:
        //
        //     Execute the REAL production method that we want to test.


        /*
         * This ACTUALLY executes HotelService.createHotel().
         *
         * createHotel() is NOT mocked.
         *
         * The "response" variable contains whatever the REAL createHotel()
         * method actually returns.
         *
         * It does NOT automatically copy or mimic our request DTO.
         *
         * The real flow should be:
         *
         *     request
         *        |
         *        v
         *     HotelService.createHotel()
         *        |
         *        | creates Hotel
         *        v
         *     hotelRepository.save(hotel)
         *        |
         *        | MOCK repository intercepts the call
         *        | gives that Hotel id = 1
         *        | returns the same Hotel
         *        v
         *     HotelService continues
         *        |
         *        | maps Hotel -> HotelResponseDTO
         *        v
         *     response
         */
        HotelResponseDTO response =
                hotelService.createHotel(request);


        // ====================================================================
        // THEN
        // ====================================================================
        //
        // THEN means:
        //
        //     Verify that the REAL HotelService behaved as expected.


        /*
         * verify(...) is from Mockito.
         *
         * IMPORTANT:
         *
         * This does NOT call save() again.
         *
         * Mockito remembers interactions with mocked objects.
         *
         * This asks Mockito:
         *
         *     "While createHotel() was executing, did it actually call
         *      hotelRepository.save(...) with a Hotel object?"
         *
         * This protects us against an incorrect implementation such as:
         *
         *     public HotelResponseDTO createHotel(HotelRequestDTO request) {
         *         // Never saves anything!
         *         return new HotelResponseDTO(
         *             1L,
         *             request.name(),
         *             request.location()
         *         );
         *     }
         *
         * The response could look correct, but createHotel() failed its
         * responsibility to persist the Hotel.
         *
         * verify(...) would catch that bug.
         *
         * By default this verifies that save(...) was called exactly once.
         */
        verify(hotelRepository).save(any(Hotel.class));


        /*
         * assertThat(...) is from AssertJ.
         *
         * These assertions check the ACTUAL HotelResponseDTO returned
         * by the REAL HotelService.createHotel() method.
         *
         * We expect ID = 1 because our mocked repository simulated
         * the database generating ID 1.
         */
        assertThat(response.id()).isEqualTo(1L);


        /*
         * Verify that the response contains the expected name.
         *
         * Because thenAnswer() returned the same Hotel that the service
         * attempted to save, a bug such as:
         *
         *     new Hotel("WRONG", request.location())
         *
         * should eventually cause this assertion to fail.
         */
        assertThat(response.name()).isEqualTo("Grand Plaza");


        /*
         * Verify that the response contains the expected location.
         */
        assertThat(response.location()).isEqualTo("Muscat");
    }

    @Test
    void createHotel_invalidRequest_fail() {
        assertThatThrownBy(() -> hotelService.createHotel(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getHotelById_returnsHotel_success() {

        // GIVEN
        // Simulate a Hotel that already exists in the database.
        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Plaza");
        hotel.setLocation("Muscat");

        // MOCKITO:
        // IF the real HotelService calls findById(1L),
        // make the mocked repository return this Hotel.
        //
        // This does NOT prove that findById() was actually called.
        when(hotelRepository.findById(1L))
                .thenReturn(Optional.of(hotel));


        // WHEN
        // Execute the REAL service method.
        HotelResponseDTO response = hotelService.getHotelById(1L);


        // THEN

        // MOCKITO:
        // Verify that the real service actually attempted to retrieve
        // Hotel ID 1 through the repository.
        //
        // verify() does NOT call findById() again.
        // It checks Mockito's recorded interaction history.
        verify(hotelRepository).findById(1L);

        // ASSERTJ:
        // Verify that the service correctly converted the Hotel returned
        // by the repository into HotelResponseDTO.
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Grand Plaza");
        assertThat(response.location()).isEqualTo("Muscat");
    }

    @Test
    void getHotelById_missingHotel_fail() {
        when(hotelRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> hotelService.getHotelById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hotel not found with id: 99");
    }

    @Test
    void getHotelById_invalidId_fail() {
//        when(hotelRepository.findById(null)).thenThrow(new IllegalArgumentException("id must not be null"));
//
//        assertThatThrownBy(() -> hotelService.getHotelById(null))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("id must not be null");
        assertThatThrownBy(() -> hotelService.getHotelById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id must not be null");

        verifyNoInteractions(hotelRepository);
    }

    @Test
    void updateHotel_updatesHotel_success() {
        /*
         * Create ONE Hotel object in memory.
         *
         * existingHotel is a variable holding a REFERENCE to that object.
         *
         * existingHotel ─────► Hotel object
         *                      id       = 1
         *                      name     = "Old Hotel"
         *                      location = "Old City"
         */
        Hotel existingHotel = new Hotel();
        existingHotel.setId(1L);
        existingHotel.setName("Old Hotel");
        existingHotel.setLocation("Old City");

        HotelRequestDTO request =
                new HotelRequestDTO("New Hotel", "New City");


        /*
         * When the service calls findById(1L), Mockito returns a reference
         * to the SAME Hotel object referenced by existingHotel.
         *
         * Mockito does NOT make a copy of existingHotel.
         *
         * Inside the service:
         *
         *     Hotel hotel = hotelRepository.findById(id).orElseThrow(...);
         *
         * means both variables now reference the SAME object:
         *
         * existingHotel ─────┐
         *                     ├────► Hotel object
         * hotel ──────────────┘      name = "Old Hotel"
         */
        when(hotelRepository.findById(1L))
                .thenReturn(Optional.of(existingHotel));


        /*
         * If save() is called with this Hotel object, return the SAME object.
         *
         * IMPORTANT JAVA OBJECT-REFERENCE CONCEPT:
         *
         * Before save() is called, the service executes:
         *
         *     hotel.setName(request.name());
         *     hotel.setLocation(request.location());
         *
         * Because "hotel" and "existingHotel" reference the SAME mutable
         * Hotel object, modifying the object through "hotel" also means
         * existingHotel now sees those modified values.
         *
         * BEFORE service modifies it:
         *
         * existingHotel ─────► Hotel { 1, "Old Hotel", "Old City" }
         *
         * AFTER service modifies it:
         *
         * existingHotel ─────┐
         *                     ├────► Hotel { 1, "New Hotel", "New City" }
         * hotel ──────────────┘
         *
         * Therefore thenReturn(existingHotel) does NOT return an old/frozen
         * copy containing "Old Hotel". It returns a reference to the SAME
         * object, which has already been modified to "New Hotel".
         */
        //A stub simply means: You tell a mock: “When this method is called with this input, return this result.”
        // This is called stubbing save().
        when(hotelRepository.save(existingHotel))
                .thenReturn(existingHotel);

        /*
         * Execute the REAL service.
         *
         * The service:
         * 1. Gets the existingHotel object from findById().
         * 2. Mutates that same object with the new name/location.
         * 3. Passes that same object to save().
         * 4. Mockito returns that same modified object from save().
         * 5. Service converts it to HotelResponseDTO.
         */
        HotelResponseDTO response =
                hotelService.updateHotel(1L, request);


        // Verify the service actually looked up Hotel 1.
        verify(hotelRepository).findById(1L);

        /*
         * Verify the service actually saved the Hotel.
         *
         * existingHotel now contains the NEW values because the service
         * mutated the same object that existingHotel references.
         */
        verify(hotelRepository).save(existingHotel);


        // Verify the DTO was created from the updated Hotel object.
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("New Hotel");
        assertThat(response.location()).isEqualTo("New City");
    }

    // NEXT
    @Test
    void updateHotel_missingHotel_fail() {
        HotelRequestDTO request = new HotelRequestDTO("New Hotel", "New City");
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.updateHotel(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hotel not found with id: 99");
    }

    @Test
    void updateHotel_invalidRequest_fail() {
        // Notice something nice here: you don't need to stub findById() anymore.
        //
        //That's because the service rejects the invalid request before touching the repository

        assertThatThrownBy(() -> hotelService.updateHotel(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("request must not be null");

        verifyNoInteractions(hotelRepository);
    }

    @Test
    void deleteHotel_deletesHotel_success() {
        when(hotelRepository.existsById(1L)).thenReturn(true);

        // Inside hotelService.deleteHotel(1L), we call hotelRepository.deleteById(id);
        // Since deleteById() returns void and you haven't stubbed it, Mockito's default behavior is simply:
        //
        //Do nothing. No database, no deletion, no exception.
        hotelService.deleteHotel(1L);

        verify(hotelRepository).deleteById(1L); // confirms that the real service did call the mock's deleteById(1L).
    }

    @Test
    void deleteHotel_missingHotel_fail() {
        when(hotelRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> hotelService.deleteHotel(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hotel not found with id: 99");
    }

    @Test
    void deleteHotel_invalidRequest_fail() {
        //Because your test still contains an old stub that is no longer being used. Null check throws it.
//        when(hotelRepository.existsById(null)).thenThrow(new IllegalArgumentException("id must not be null"));

        assertThatThrownBy(() -> hotelService.deleteHotel(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id must not be null");
    }
}