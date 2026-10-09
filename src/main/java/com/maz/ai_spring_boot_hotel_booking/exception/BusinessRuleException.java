package com.maz.ai_spring_boot_hotel_booking.exception;

/**
 * Thrown by SERVICES when a request is well-formed but violates a business
 * rule, e.g. "check-out date must be after check-in date".
 *
 * Why a custom exception instead of IllegalArgumentException?
 * The GlobalExceptionHandler dispatches by TYPE, not by message. Tons of
 * library code also throws IllegalArgumentException; if we handled that type
 * as 400, a library-thrown one (really a bug in OUR code, deserving 500)
 * would be mislabeled as a client error, its internal message leaked to the
 * client, and logged at WARN instead of ERROR.
 *
 * A custom type is airtight: only OUR service code can throw
 * BusinessRuleException, so type-based dispatch is guaranteed correct.
 * Bonus: grep for this class name and you find every business rule in the app.
 *
 * Unchecked (extends RuntimeException) for the same reason as
 * ResourceNotFoundException: services don't have to declare it, and it
 * propagates up to the GlobalExceptionHandler automatically.
 */
// BusinessRuleException is used for throwing our own customer error
// whenever a request violates a rule of the business domain - a rule
// that lives in our heads/specs, not in annotations or the DB schema.
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
