package com.maz.ai_spring_boot_hotel_booking.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response body returned by the global exception handler.
 *
 * Every error from this API has the same JSON shape, e.g.:
 * {
 *   "timestamp": "2026-09-03T08:15:30.123",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Hotel not found with id: 99",
 *   "fieldErrors": null
 * }
 *
 * fieldErrors is only populated for validation failures (400):
 * {
 *   ...
 *   "message": "Validation failed",
 *   "fieldErrors": { "name": "Hotel name is required", "price": "Price must be greater than zero" }
 * }
 *
 * A consistent error contract lets API clients write ONE error-parsing
 * routine instead of guessing per endpoint.
 */
// One JSON shape for every error the API produces
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors
) {

    // ===== STATIC FACTORY METHODS =====
    //
    // "Factory" is not a keyword or annotation - it's a design-pattern label for
    // any method whose job is to CREATE and RETURN an instance of the class,
    // instead of callers using `new` directly. A factory manufactures objects;
    // callers ask the factory rather than assembling the product themselves.
    //
    // What makes a method a factory:
    //   1. static            -> callable on the class itself: ErrorResponse.of(...)
    //   2. returns instance  -> `return new ErrorResponse(...)`
    //   3. hides assembly    -> fills in the boring/error-prone parts for the caller
    //
    // Why we use them here instead of the raw constructor:
    //   - the constructor demands ALL 5 fields:
    //       new ErrorResponse(LocalDateTime.now(), 404, "Not Found", "msg", null)
    //   - callers could forget LocalDateTime.now() (bug) or must pass an ugly null
    //   - the factory sets the timestamp in ONE place and gives the call a
    //     readable name: ErrorResponse.of(404, "Not Found", "msg")
    //
    // Well-known JDK factories you already use:
    //   LoggerFactory.getLogger(...), ResponseEntity.status(...), LocalDate.of(...)
    //
    // Contrast - a static method that is NOT a factory (computes, creates nothing):
    //   public static boolean isServerError(ErrorResponse r) { return r.status() >= 500; }

    /**
     * Factory for simple errors WITHOUT per-field details (e.g. 404 Not Found).
     * Sets timestamp = now automatically and fieldErrors = null.
     * "of" is the JDK's conventional factory name: "make me one of these".
     */
    // A static method is just a method — "of" has zero special meaning to the compiler.
    // ErrorResponse is the return type of the method "of"
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, null);
    }

    /**
     * Factory for validation errors (400) WITH per-field messages,
     * e.g. { "name": "Hotel name is required" }.
     * Named ofValidation to distinguish it from the simpler of() above -
     * factory names can carry meaning, unlike overloaded constructors.
     */
    public static ErrorResponse ofValidation(int status, String error, String message,
                                             Map<String, String> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, fieldErrors);
    }
}
