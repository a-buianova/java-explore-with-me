package ru.practicum.ewm.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

/**
 * Centralized exception handling producing ApiError in the format required by the spec.
 */
@Slf4j
@RestControllerAdvice(basePackages = "ru.practicum.ewm")
public class GlobalExceptionHandler {

    private static final String REASON_BAD_REQUEST = "Incorrectly made request.";
    private static final String REASON_NOT_FOUND   = "The required object was not found.";
    private static final String REASON_CONFLICT    = "Integrity constraint has been violated.";
    private static final String REASON_INTERNAL    = "An unexpected error occurred.";

    /** Maps 404 to spec-compliant ApiError. */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        log.warn("404 Not Found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, REASON_NOT_FOUND, ex.getMessage());
    }

    /** Maps 409 (business/integrity conflicts). */
    @ExceptionHandler({ ConflictException.class, DataIntegrityViolationException.class })
    public ResponseEntity<ApiError> handleConflict(Exception ex) {
        String msg = (ex instanceof ConflictException) ? ex.getMessage() : "Data integrity violation";
        log.warn("409 Conflict: {}", msg);
        return build(HttpStatus.CONFLICT, REASON_CONFLICT, msg);
    }

    /** Maps explicit 400 (manual validation). */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        log.warn("400 Bad Request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, REASON_BAD_REQUEST, ex.getMessage());
    }

    /**
     * Maps framework validation errors to 400.
     * Returns a generic message to avoid leaking internal details.
     */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiError> handleValidationErrors(Exception ex) {
        log.warn("400 Validation error: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, REASON_BAD_REQUEST, "Invalid request");
    }

    /** Maps IllegalArgumentException (e.g., invalid from/size) to 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("400 IllegalArgument: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, REASON_BAD_REQUEST, "Invalid request");
    }

    /** Last-resort 500 handler. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("500 Internal error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, REASON_INTERNAL, REASON_INTERNAL);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String reason, String message) {
        ApiError body = ApiError.builder()
                .status(status.name())
                .reason(reason)
                .message(message != null ? message : reason)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}