package ru.practicum.ewm.event.controller.pub;

import jakarta.servlet.http.HttpServletRequest;
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
import java.util.List;

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
    public List<EventShortDto> search(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size,
            HttpServletRequest request
    ) {

        Collection<Long> normalizedCats = (categories == null || categories.isEmpty()) ? null : categories;

        return service.searchPublic(
                text,
                normalizedCats,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size,
                request
        ).getContent();
    }

    /** Returns detailed information about a specific published event. */
    @GetMapping("/{eventId}")
    public EventFullDto getById(@PathVariable long eventId,
                                HttpServletRequest request) {
        return service.getPublicById(eventId, request);
    }
}