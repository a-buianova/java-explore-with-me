package ru.practicum.ewm.event.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Admin API for moderation and management of all events.
 * <p>
 * Endpoints:
 * - GET /admin/events — search events by filters
 * - PUT /admin/events/{eventId} — publish or reject event
 */
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class EventAdminController {

    private final EventService service;

    /** Returns events by filters: users, states, categories, date range, pagination. */
    @GetMapping
    public Collection<EventFullDto> searchAdmin(
            @RequestParam(required = false) Collection<Long> users,
            @RequestParam(required = false) Collection<String> states,
            @RequestParam(required = false) Collection<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        return service.searchAdmin(users, states, categories, rangeStart, rangeEnd, from, size).getContent();
    }

    /** Publishes or rejects an event by admin action. */
    @PutMapping("/{eventId}")
    public EventFullDto updateByAdmin(@PathVariable long eventId,
                                      @RequestBody @Valid UpdateEventAdminRequest dto) {
        return service.updateByAdmin(eventId, dto);
    }
}