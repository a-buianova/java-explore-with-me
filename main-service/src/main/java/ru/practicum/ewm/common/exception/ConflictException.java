package ru.practicum.ewm.common.exception;

/** Thrown when a business rule or integrity constraint is violated (HTTP 409). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}