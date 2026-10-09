package com.maz.ai_spring_boot_hotel_booking.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the whole API.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody:
 * - wraps ALL @RestController classes in the application
 * - any exception thrown by any endpoint (or by validation before the
 *   endpoint runs) is routed to the matching @ExceptionHandler method here
 * - return values are serialized to JSON, same as in controllers
 *
 *
 * When a request arrives, Spring does work before your method body ever executes:
 * 1. Deserialize — Jackson turns the JSON into a HotelRequestDTO
 * 2. Validate — because of @Valid, Spring checks all the annotations on the DTO (@NotBlank, @Positive, ...)
 * 3. Only if validation passes → your method body finally runs
 * If step 2 fails, Spring throws MethodArgumentNotValidException itself — your createHotel body is never entered.
 * Not a single line of your code ran, yet an exception was thrown.
 *
 *
 * That's why the comment says "or by validation before the endpoint runs": the handler catches two kinds of exceptions —
 * • ones thrown inside your code (service throws ResourceNotFoundException, DB rejects an insert...)
 * • ones thrown by Spring itself, before your code even starts (validation failure)
 * Both happen within the request-handling pipeline, so both funnel to GlobalExceptionHandler.
 * It's also why we never write throw new MethodArgumentNotValidException(...) anywhere — Spring
 * is the only one who throws it.
 *
 *
 * This is why controllers and services contain zero try/catch blocks:
 * they throw, this class translates exception -> HTTP response in ONE place.
 *
 * ============================================================================
 * HOW EXCEPTIONS FROM *ANY* LAYER END UP HERE (the mental model)
 * ============================================================================
 *
 * @RestControllerAdvice only watches CONTROLLERS - but that is enough to catch
 * everything, because of how Java exceptions travel: an uncaught exception
 * always climbs UP the call chain, caller by caller, until something catches it.
 *
 * Every HTTP request builds this call chain:
 *
 *      Controller   <-- watched by GlobalExceptionHandler
 *         | calls
 *      Service
 *         | calls
 *      Repository
 *         | talks to
 *      Database     <-- where @Entity constraints (unique, not-null) live
 *
 * If the DB rejects an insert (e.g. duplicate roomNumber), the exception is
 * thrown at the BOTTOM and bubbles up: repository -> service -> controller.
 * Nothing catches it on the way (we wrote no try/catch on purpose). The moment
 * it pops out of the controller, Spring's DispatcherServlet grabs it and hands
 * it to the matching @ExceptionHandler method in this class.
 *
 * So this class does NOT watch entities, services, or repositories directly -
 * it watches the EXIT DOOR (the controller), and every exception eventually
 * walks out through that door, because the controller is where every request
 * started.
 *
 * What about @Entity? Entities never throw exceptions themselves - they are
 * passive data holders. The DATABASE rejects the operation; Hibernate/Spring
 * wrap that rejection into DataIntegrityViolationException; and it bubbles up
 * the same chain like everything else.
 *
 * One-line summary:
 *   Exceptions bubble up; all requests enter through a controller;
 *   so guarding controllers guards everything below them.
 * ============================================================================
 */
@RestControllerAdvice // the controller is the single doorway. @RestControllerAdvice watches controllers only. But that's enough to catch everything, because of how exceptions travel
public class GlobalExceptionHandler { // You can name the class anything you want, but "GlobalExceptionHandler" is a common convention. The correlation between global exception and controllers is the annotation

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final Environment environment;

    /**
     * Spring calls this constructor automatically when it creates the
     * GlobalExceptionHandler bean during application startup.
     *
     * Why does it happen?
     * - @RestControllerAdvice is a Spring-managed component.
     * - Spring scans for beans in the application context.
     * - For this class, it detects the constructor parameter Environment and
     *   injects the current Spring Environment object before the bean is used.
     *
     * When does it happen?
     * - Once, when the Spring application context starts.
     * - Not per request. The bean is reused for all incoming HTTP requests.
     *
     * This is constructor injection: a preferred Spring pattern because the
     * bean has a guaranteed dependency at creation time.
     */
    // This part helps us know which environment we are running in (dev, staging, prod) so we can decide how much
    // information to return to the client in case of an unexpected error. In dev/staging we can return more details,
    // in prod we return a generic message. We dictate the enviroment based on the env key in the environment vars
    // SPRING_PROFILES_ACTIVE is the environment variable that determines the current active profile (e.g., dev, staging, prod).
    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    /**
     * Thrown by services via orElseThrow when an id lookup fails.
     * Translated to HTTP 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        // WARN, not ERROR: a client asking for a missing id is expected
        // traffic, not a system failure.
        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),          // 404
                HttpStatus.NOT_FOUND.getReasonPhrase(), // "Not Found"
                ex.getMessage());                       // "Hotel not found with id: 99"

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Thrown by Spring automatically when @Valid fails on a request body -
     * BEFORE the controller method runs. We never throw this ourselves.
     *
     * Translated to HTTP 400 Bad Request with one message per invalid field,
     * taken from the DTO annotations (e.g. "Hotel name is required").
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Collect every field violation: fieldName -> message.
        // getBindingResult() holds all failures, not just the first one,
        // so the client can fix everything in a single round trip.
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage()));

        log.warn("Validation failed: {}", fieldErrors);

        ErrorResponse body = ErrorResponse.ofValidation(
                HttpStatus.BAD_REQUEST.value(),           // 400
                HttpStatus.BAD_REQUEST.getReasonPhrase(),  // "Bad Request"
                "Validation failed",
                fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Thrown by Spring when a database constraint rejects an operation:
     * UNIQUE violated (duplicate room number), NOT NULL violated,
     * foreign key violated, etc.
     *
     * Spring translates each database's native error (PostgreSQL, MySQL...)
     * into this one portable exception - part of its DataAccessException
     * hierarchy - so we handle ONE type regardless of the DB vendor.
     *
     * Translated to HTTP 409 Conflict: the request conflicts with the
     * current state of the data.
     *
     * Note: we log the technical details but return a GENERIC message.
     * Raw constraint-violation text leaks schema internals (table/column/
     * constraint names) - useful to attackers, useless to clients.
     *
     * DataIntegrityViolationException catches one category of DB exceptions: constraint
     * violations — unique violated, not-null violated, foreign key violated. "Your data
     * broke a rule the schema enforces." It does NOT cover all DB problems. Spring has
     * a whole hierarchy, DataAccessException, with
     * subclasses per failure category:
     *
     * DataAccessException                    (root — all DB problems)
     * ├── DataIntegrityViolationException    (constraint broken)      → we handle: 409
     * ├── CannotAcquireLockException         (deadlock/lock timeout)
     * ├── QueryTimeoutException              (query too slow)
     * ├── DataAccessResourceFailureException (DB down / connection lost)
     * └── ... many more
     *
     * WHY 409 FOR EVERYTHING, EVEN A MISSING REQUIRED VALUE?
     * Strictly speaking, 409 Conflict only truly fits the DUPLICATE case
     * (unique violated). A NOT NULL violation would be more accurately a
     * 400 Bad Request ("you sent incomplete data"). We still lump both into
     * 409, and that is fine, because:
     *
     * 1. A missing value should NEVER reach the database. Our DTOs carry
     *    @NotBlank / @NotNull, so a missing field is caught at the front
     *    door by @Valid and returned as a proper 400 by the handler above MethodArgumentNotValidException.
     * 2. The nullable=false column in the DB is only a LAST LINE OF DEFENSE
     *    (defense in depth). If it ever fires, it means we FORGOT a
     *    validation annotation on a DTO - the fix is to add the annotation,
     *    not to make this handler smarter.
     * 3. So in a well-validated API, this handler is almost always hit by
     *    the duplicate case - the one thing DTO validation CANNOT catch,
     *    because it requires checking existing rows in the DB. 409 is the
     *    right code for that.
     *
     * We could branch on the root cause message to return 400 vs 409, but
     * that means string-matching vendor-specific text (PostgreSQL wording
     * differs from MySQL...) - brittle and rarely worth it.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // getMostSpecificCause() digs to the root DB error for the log
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),            // 409
                HttpStatus.CONFLICT.getReasonPhrase(),   // "Conflict"
                "Operation violates a data integrity constraint (e.g. duplicate or missing required value)");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Thrown by services for BUSINESS-RULE violations, e.g. BookingService
     * rejecting a check-out date that is not after the check-in date.
     *
     * Why an explicit handler? Without it, this fell into the catch-all below
     * and returned 500 ("our server broke") - but the truth is the CLIENT sent
     * bad data, so 400 Bad Request is the honest status. This is the general
     * pattern: promote known, expected exceptions out of the catch-all into
     * their own handler with the correct status code.
     *
     * The exception message is safe to return: it is our own business-rule
     * text, not internals.
     *
     * Why BusinessRuleException and NOT IllegalArgumentException?
     * Dispatch here is by TYPE - a handler can't read messages to decide
     * "is this one ours?". Library code all over the JDK/Spring also throws
     * IllegalArgumentException; handling that type as 400 would mislabel a
     * genuine bug in our code (deserves 500 + ERROR log) as a client error,
     * and leak the library's internal message to the client. Our own custom
     * type can ONLY be thrown by our services, so 400 is always correct.
     * Library IllegalArgumentExceptions now fall through to the catch-all
     * below - 500, ERROR log, generic message - exactly what a bug deserves.
     *
     * CONCRETE EXAMPLE of the "mislabeled bug" problem we avoided:
     * Suppose we one day add this buggy line to a service:
     *
     *     DateTimeFormatter fmt = DateTimeFormatter.ofPattern("XYZ"); // invalid pattern!
     *
     * The JDK throws: IllegalArgumentException("Unknown pattern letter: X").
     * Whose fault? OURS - the client's request was perfectly fine sent with a valid date.
     * The honest response is 500 ("our server has a problem") + ERROR log
     * so the bug screams at us and we go fix it.
     *
     * But if we had a handler for IllegalArgumentException -> 400, it could
     * not tell these two apart (same type, and dispatch ignores messages):
     *   - IllegalArgumentException("Check-out date must be after check-in")  <- ours, client's fault
     *   - IllegalArgumentException("Unknown pattern letter: X")              <- JDK's, OUR fault
     * Both would get 400. The client would receive
     *   { "status": 400, "message": "Unknown pattern letter: X" }
     * which LIES: 400 means "you sent bad data", blaming the client for our
     * bug. The client wastes time re-checking a fine request, and the WARN
     * log means our team never notices the bug.
     *
     * With BusinessRuleException the confusion is impossible: the JDK can
     * never throw OUR class, so this 400 handler only fires for genuine
     * client mistakes. Right blame, right log level, every time.
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        // WARN, not ERROR: a client mistake, not a system failure
        log.warn("Business rule violated: {}", ex.getMessage());

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),           // 400
                HttpStatus.BAD_REQUEST.getReasonPhrase(),  // "Bad Request"
                ex.getMessage()); // e.g. "Check-out date must be after check-in date"

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * CATCH-ALL: any exception no other handler in this class matched.
     *
     * Spring picks the MOST SPECIFIC handler first (like try/catch ordering):
     * ResourceNotFoundException still hits its own 404 handler above even
     * though it is also an Exception - this method only fires as a LAST RESORT.
     *
     * Why have it?
     * - Even unexpected bugs return our consistent JSON ErrorResponse shape,
     *   instead of Spring's default error body.
     * - We get the FULL stack trace in our logs: log.error("...", ex) with the
     *   exception as the last argument logs the whole trace.
     *
     * Translated to HTTP 500 Internal Server Error: a 500 means OUR code
     * failed, not the client. Log at ERROR (a real failure, unlike the WARNs
     * above), but respond with a GENERIC message - never leak stack traces or
     * internals to clients.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex); // full stack trace goes to the log

        String[] activeProfiles = environment.getActiveProfiles();
        boolean isDevOrStaging = false;
        for (String profile : activeProfiles) {
            if ("dev".equals(profile) || "staging".equals(profile)) {
                isDevOrStaging = true;
                break;
            }
        }

        String message = "An unexpected error occurred";
        if (isDevOrStaging) {
            if (!ex.getClass().getName().isBlank() || !ex.getMessage().isBlank()) {
                message = "Exception: " + ex.getClass().getName() + " | Message: " + ex.getMessage();
            }
        }

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),            // 500
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),   // "Internal Server Error"
                message);                      // generic on purpose

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
