package ru.practicum.ewm.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for public event search parameters.
 * Collects all query parameters from the /events endpoint into one object.
 * Simplifies controller method signatures and improves readability.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicEventSearchRequest {

    /** Text to search in annotation or description (optional). */
    private String text;

    /** List of category IDs for filtering (optional). */
    private List<Long> categories;

    /** Whether to include only paid events (optional). */
    private Boolean paid;

    /** Start of the date range (defaults to now if null). */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    /** End of the date range (optional). */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    /** Whether to show only events with available participant slots. */
    @Builder.Default
    private boolean onlyAvailable = false;

    /** Sorting criteria: EVENT_DATE or VIEWS (optional). */
    private String sort;

    /** Pagination offset (default: 0). */
    @Builder.Default
    private int from = 0;

    /** Pagination size (default: 10). */
    @Builder.Default
    private int size = 10;
}