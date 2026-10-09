package com.maz.ai_spring_boot_hotel_booking.exception;

/**
 * Thrown when a requested resource (Hotel, Room, Booking) does not exist
 * in the database - e.g. GET /hotels/99 when there is no hotel with id 99.
 *
 * Extends RuntimeException (unchecked) rather than Exception (checked):
 * - Unchecked exceptions don't force every caller to declare "throws ..."
 *   up the call chain (controller -> service -> repository would all need it)
 * - Spring's transaction management rolls back on unchecked exceptions by default
 * - The global exception handler (@RestControllerAdvice, Step 7) will catch it
 *   anywhere in the request flow and convert it to an HTTP 404 response
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * @param message human-readable description returned to the API client,
     *                e.g. "Hotel not found with id: 99"
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
