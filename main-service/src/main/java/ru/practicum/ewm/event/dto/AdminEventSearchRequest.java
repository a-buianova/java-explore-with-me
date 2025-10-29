package ru.practicum.ewm.event.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for admin event search.
 * Combines all filter parameters (users, states, categories, date range, pagination)
 * to reduce controller/service parameter count.
 */
@Data
public class AdminEventSearchRequest {

    /** Filter by initiator IDs (optional). */
    private List<Long> users;

    /** Filter by event lifecycle states (PENDING / PUBLISHED / CANCELED). */
    private List<String> states;

    /** Filter by category IDs (optional). */
    private List<Long> categories;

    /** Start of date range for eventDate filter. */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    /** End of date range for eventDate filter. */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    /** Pagination offset (default = 0). */
    private int from = 0;

    /** Pagination size limit (default = 10). */
    private int size = 10;
}