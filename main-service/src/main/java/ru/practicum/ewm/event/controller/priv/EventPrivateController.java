package ru.practicum.ewm.event.controller.priv;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.service.EventService;

import java.util.Collection;

/**
 * Private API for event initiators (authenticated users).
 * <p>
 * Endpoints:
 * - POST /users/{userId}/events — create event
 * - PATCH /users/{userId}/events/{eventId} — update own event
 * - GET /users/{userId}/events — list user’s events
 * - GET /users/{userId}/events/{eventId} — get detailed info
 */
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class EventPrivateController {

    private final EventService service;

    /** Creates a new event for the given user. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable long userId,
                               @RequestBody @Valid NewEventDto dto) {
        return service.create(userId, dto);
    }

    /** Updates the user’s own event. */
    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable long userId,
                               @PathVariable long eventId,
                               @RequestBody @Valid UpdateEventUserRequest dto) {
        return service.updateByInitiator(userId, eventId, dto);
    }

    /** Returns all events created by the user. */
    @GetMapping
    public Collection<EventShortDto> listMine(@PathVariable long userId,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                              @RequestParam(defaultValue = "10") @Positive int size) {
        return service.findByInitiator(userId, from, size).getContent();
    }

    /** Returns detailed info about one of the user’s events. */
    @GetMapping("/{eventId}")
    public EventFullDto getMine(@PathVariable long userId,
                                @PathVariable long eventId) {
        return service.getByInitiator(userId, eventId);
    }
}