package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import ru.practicum.ewm.event.dto.*;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Event service interface.
 * Defines all public, private and admin operations for event management.
 */
public interface EventService {

    /** Creates a new event in PENDING state. */
    EventFullDto create(long userId, NewEventDto dto);

    /** Updates an existing event by its initiator (allowed only for PENDING or CANCELED states). */
    EventFullDto updateByInitiator(long userId, long eventId, UpdateEventUserRequest dto);

    /** Returns events created by a specific user with offset pagination. */
    Page<EventShortDto> findByInitiator(long userId, int from, int size);

    /** Returns detailed info about a specific event owned by the initiator. */
    EventFullDto getByInitiator(long userId, long eventId);

    /** Public search for published events using raw parameters (legacy version). */
    Page<EventShortDto> searchPublic(String text,
                                     Collection<Long> categories,
                                     Boolean paid,
                                     LocalDateTime rangeStart,
                                     LocalDateTime rangeEnd,
                                     boolean onlyAvailable,
                                     String sort,
                                     int from,
                                     int size,
                                     HttpServletRequest request);

    /**
     * Public search for published events using a structured request DTO.
     * Cleaner alternative to the legacy parameter-based method.
     */
    Page<EventShortDto> searchPublic(PublicEventSearchRequest req, HttpServletRequest request);

    /** Returns detailed information about a published event (hit logged to stats). */
    EventFullDto getPublicById(long eventId, HttpServletRequest request);

    /** Admin search for events using multiple filters (users, states, categories, date range). */
    Page<EventFullDto> searchAdmin(Collection<Long> users,
                                   Collection<String> states,
                                   Collection<Long> categories,
                                   LocalDateTime rangeStart,
                                   LocalDateTime rangeEnd,
                                   int from,
                                   int size);

    /**
     * Admin search for events using a structured request DTO.
     * Replaces multiple parameters with a single object.
     */
    Page<EventFullDto> searchAdmin(AdminEventSearchRequest req);

    /** Updates or moderates an event by an administrator (publish/reject). */
    EventFullDto updateByAdmin(long eventId, UpdateEventAdminRequest dto);
}