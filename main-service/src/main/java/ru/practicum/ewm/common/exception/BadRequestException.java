package ru.practicum.ewm.common.exception;

/** Thrown when request parameters or payload are invalid (HTTP 400). */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}