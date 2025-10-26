package ru.practicum.ewm.common.exception;

/** Thrown when a requested resource is not found (HTTP 404). */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}