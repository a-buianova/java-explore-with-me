package ru.practicum.ewm.request.model;

/**
 * Lifecycle state of a participation request.
 */
public enum RequestStatus {
    /** Request is waiting for review. */
    PENDING,
    /** Request has been confirmed by organizer. */
    CONFIRMED,
    /** Request was rejected by organizer. */
    REJECTED,
    /** Request canceled by the user. */
    CANCELED
}