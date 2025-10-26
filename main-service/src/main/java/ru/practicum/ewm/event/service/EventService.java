package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import ru.practicum.ewm.event.dto.*;
import java.time.LocalDateTime;
import java.util.*;

/** Public, private and admin operations for events. */
public interface EventService {

    EventFullDto create(long userId, NewEventDto dto);

    EventFullDto updateByInitiator(long userId, long eventId, UpdateEventUserRequest dto);

    Page<EventShortDto> findByInitiator(long userId, int from, int size);

    EventFullDto getByInitiator(long userId, long eventId);

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

    EventFullDto getPublicById(long eventId, HttpServletRequest request);

    Page<EventFullDto> searchAdmin(Collection<Long> users,
                                   Collection<String> states,
                                   Collection<Long> categories,
                                   LocalDateTime rangeStart,
                                   LocalDateTime rangeEnd,
                                   int from,
                                   int size);

    EventFullDto updateByAdmin(long eventId, UpdateEventAdminRequest dto);
}