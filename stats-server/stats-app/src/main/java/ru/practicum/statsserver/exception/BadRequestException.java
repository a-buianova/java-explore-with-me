package ru.practicum.statsserver.exception;

/** 400 Bad Request for invalid input parameters. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}