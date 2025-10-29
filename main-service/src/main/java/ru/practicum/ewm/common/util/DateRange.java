package ru.practicum.ewm.common.util;

import java.time.LocalDateTime;

/**
 * Simple immutable date range with basic validation (end must be >= start).
 * Can be used for filtering queries.
 */
public record DateRange(LocalDateTime start, LocalDateTime end) {

    public DateRange {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("end must be equal to or after start");
        }
    }

    /** Factory with validation. */
    public static DateRange of(LocalDateTime start, LocalDateTime end) {
        return new DateRange(start, end);
    }
}