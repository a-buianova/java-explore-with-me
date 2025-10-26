package ru.practicum.ewm.event.controller.pub;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Public API for browsing published events.
 * <p>
 * Endpoints:
 * - GET /events — search and filter published events
 * - GET /events/{eventId} — get full details of a published event
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class EventPublicController {

    private final EventService service;

    /** Returns a list of published events with filtering and pagination. */
    @GetMapping
    public Collection<EventShortDto> search(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Collection<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort, // EVENT_DATE | VIEWS
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        return service.searchPublic(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size, null).getContent();
    }

    /** Returns detailed information about a specific published event. */
    @GetMapping("/{eventId}")
    public EventFullDto getById(@PathVariable long eventId) {
        return service.getPublicById(eventId, null);
    }
}