package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.common.exception.BadRequestException;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.common.util.PageUtil;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.StateAction;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Event use-cases: create/update, public search with views, admin moderation.
 * Integrates with the stats service for hits and view counters.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final Set<String> SORT_ALLOWED = Set.of("EVENT_DATE", "VIEWS");

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;

    /**
     * Create a new event in PENDING state. Event date must be ≥ 2h from now.
     */
    @Override
    @Transactional
    public EventFullDto create(long userId, NewEventDto dto) {
        User initiator = getUserOrThrow(userId);
        Category category = getCategoryOrThrow(dto.getCategory());
        validateEventDateAtLeast(dto.getEventDate(), 2);

        Event entity = EventMapper.toEntity(dto, category, initiator);
        entity.setState(EventState.PENDING);
        entity.setCreatedOn(LocalDateTime.now());
        entity.setPublishedOn(null);
        entity.setConfirmedRequests(0);

        Event saved = eventRepository.save(entity);
        log.info("Created event id={} by user={}", saved.getId(), userId);
        return EventMapper.toFullDto(saved, 0L);
    }

    /**
     * Update by initiator. Allowed only for PENDING or CANCELED.
     * Supports SEND_TO_REVIEW / CANCEL_REVIEW via stateAction (String).
     */
    @Override
    @Transactional
    public EventFullDto updateByInitiator(long userId, long eventId, UpdateEventUserRequest dto) {
        Event event = getOwnedEventOrThrow(userId, eventId);
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be updated by initiator");
        }

        Category newCategory = (dto.getCategory() != null) ? getCategoryOrThrow(dto.getCategory()) : null;
        EventMapper.applyUserPatch(event, dto, newCategory);

        if (dto.getStateAction() != null) {
            StateAction action = parseStateAction(dto.getStateAction());
            if (action == StateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (action == StateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            } else {
                throw new BadRequestException("Unsupported stateAction for user: " + action);
            }
        }

        if (dto.getEventDate() != null) {
            validateEventDateAtLeast(dto.getEventDate(), 2);
        }

        Event saved = eventRepository.save(event);
        return EventMapper.toFullDto(saved, 0L);
    }

    /**
     * List initiator’s events with offset pagination.
     */
    @Override
    public Page<EventShortDto> findByInitiator(long userId, int from, int size) {
        getUserOrThrow(userId);
        var pageable = PageUtil.byFromSize(from, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        return eventRepository.findAllByInitiatorId(userId, pageable)
                .map(e -> EventMapper.toShortDto(e, 0L));
    }

    /**
     * Get a single event owned by initiator.
     */
    @Override
    public EventFullDto getByInitiator(long userId, long eventId) {
        Event event = getOwnedEventOrThrow(userId, eventId);
        return EventMapper.toFullDto(event, 0L);
    }

    /**
     * Public search: filters + pagination + sorting (EVENT_DATE/VIEWS).
     * - Logs a hit to stats service.
     * - Enriches results with view counters (unique=true).
     * - Applies onlyAvailable (capacity-based) filter in-memory.
     */
    @Override
    public Page<EventShortDto> searchPublic(
            String text,
            Collection<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            boolean onlyAvailable,
            String sort,
            int from,
            int size,
            HttpServletRequest request
    ) {
        safeSendHit(request);

        LocalDateTime start = (rangeStart == null) ? LocalDateTime.now() : rangeStart;
        if (rangeEnd != null && rangeEnd.isBefore(start)) {
            throw new BadRequestException("end must be equal to or after start");
        }

        Sort srt = Sort.unsorted();
        boolean sortByViews = false;
        if (sort != null) {
            String upper = sort.toUpperCase(Locale.ROOT);
            if (!SORT_ALLOWED.contains(upper)) {
                throw new BadRequestException("Unsupported sort: " + sort);
            }
            if (upper.equals("EVENT_DATE")) {
                srt = Sort.by(Sort.Direction.ASC, "eventDate");
            } else {
                sortByViews = true; // sort by views after enrichment
            }
        }

        var pageable = PageUtil.byFromSize(from, size, srt);
        Page<Event> page = eventRepository.searchPublic(text, categories, paid, start, rangeEnd, pageable);

        List<Event> content = page.getContent();
        if (onlyAvailable) {
            content = content.stream()
                    .filter(e -> e.getParticipantLimit() == 0 || e.getConfirmedRequests() < e.getParticipantLimit())
                    .collect(Collectors.toList());
        }

        Map<Long, Long> viewsMap = fetchViewsFor(content, start, (rangeEnd == null ? LocalDateTime.now() : rangeEnd));

        if (sortByViews) {
            content.sort(Comparator.comparingLong((Event e) -> viewsMap.getOrDefault(e.getId(), 0L)).reversed());
        }

        List<EventShortDto> mapped = content.stream()
                .map(e -> EventMapper.toShortDto(e, viewsMap.getOrDefault(e.getId(), 0L)))
                .toList();

        return new PageImpl<>(mapped, pageable, page.getTotalElements());
    }

    /**
     * Public read: event must be PUBLISHED. Logs a hit and enriches views.
     */
    @Override
    public EventFullDto getPublicById(long eventId, HttpServletRequest request) {
        safeSendHit(request);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found or not published"));

        Map<Long, Long> views = fetchViewsFor(List.of(event), event.getCreatedOn(), LocalDateTime.now());
        return EventMapper.toFullDto(event, views.getOrDefault(eventId, 0L));
    }

    /**
     * Admin search with filters and date range validation.
     */
    @Override
    public Page<EventFullDto> searchAdmin(
            Collection<Long> users,
            Collection<String> states,
            Collection<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size
    ) {
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("end must be equal to or after start");
        }
        var pageable = PageUtil.byFromSize(from, size, Sort.by(Sort.Direction.DESC, "createdOn"));

        Collection<EventState> stateEnums = parseStates(states);
        Page<Event> page = eventRepository.searchAdmin(users, stateEnums, categories, rangeStart, rangeEnd, pageable);
        return page.map(e -> EventMapper.toFullDto(e, 0L));
    }

    /**
     * Admin patch + moderation. Supports PUBLISH_EVENT / REJECT_EVENT.
     */
    @Override
    @Transactional
    public EventFullDto updateByAdmin(long eventId, UpdateEventAdminRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        Category newCategory = (dto.getCategory() != null) ? getCategoryOrThrow(dto.getCategory()) : null;
        EventMapper.applyAdminPatch(event, dto, newCategory);

        if (dto.getEventDate() != null) {
            validateEventDateNotPast(dto.getEventDate());
        }

        if (dto.getStateAction() != null) {
            StateAction action = parseStateAction(dto.getStateAction());
            if (action == StateAction.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Only pending events can be published");
                }
                if (event.getEventDate() != null &&
                        event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException("Event date must be at least 1 hour after publish time");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (action == StateAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Published events cannot be rejected");
                }
                event.setState(EventState.CANCELED);
            } else {
                throw new BadRequestException("Unsupported stateAction for admin: " + action);
            }
        }

        Event saved = eventRepository.save(event);
        return EventMapper.toFullDto(saved, 0L);
    }

    // helpers

    /**
     * Sends a hit to the statistics service.
     * Fails silently if stats service is unavailable.
     */
    private void safeSendHit(HttpServletRequest request) {
        try {
            statsClient.sendHit(request);
        } catch (Exception ex) {
            log.warn("stats sendHit failed: {}", ex.getMessage());
        }
    }

    /**
     * Fetches unique view counts for given events within a time range.
     * Returns zeroes for missing entries or errors.
     */
    private Map<Long, Long> fetchViewsFor(Collection<Event> events, LocalDateTime start, LocalDateTime end) {
        if (events == null || events.isEmpty()) return Collections.emptyMap();

        List<String> uris = events.stream()
                .map(Event::getId)
                .filter(Objects::nonNull)
                .map(id -> "/events/" + id)
                .toList();

        try {
            List<ViewStats> stats = statsClient.getStats(start, end, uris, true);
            Map<Long, Long> map = new HashMap<>();
            if (stats != null) {
                for (ViewStats vs : stats) {
                    Long id = extractId(vs.getUri());
                    if (id != null) map.merge(id, vs.getHits(), Long::sum);
                }
            }
            for (String u : uris) {
                Long id = extractId(u);
                if (id != null) map.putIfAbsent(id, 0L);
            }
            return map;
        } catch (Exception ex) {
            log.warn("stats getStats failed: {}", ex.getMessage());
            Map<Long, Long> fallback = new HashMap<>();
            for (Event e : events) if (e.getId() != null) fallback.put(e.getId(), 0L);
            return fallback;
        }
    }

    /**
     * Extracts numeric event ID from a URI like "/events/{id}".
     */
    private static Long extractId(String uri) {
        if (uri == null) return null;
        String[] p = uri.split("/");
        if (p.length >= 3 && "events".equals(p[1])) {
            try {
                return Long.parseLong(p[2]);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    /**
     * Parses a list of string state names into EventState enums.
     */
    private Collection<EventState> parseStates(Collection<String> states) {
        if (states == null) return null;
        try {
            return states.stream()
                    .filter(Objects::nonNull)
                    .map(s -> EventState.valueOf(s.trim().toUpperCase(Locale.ROOT)))
                    .collect(Collectors.toSet());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid state value in filter: " + states);
        }
    }

    /**
     * Fetches user or throws 404 if missing.
     */
    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    /**
     * Fetches category or throws 404 if missing.
     */
    private Category getCategoryOrThrow(long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));
    }

    /**
     * Fetches event owned by given user, throws 404 otherwise.
     */
    private Event getOwnedEventOrThrow(long userId, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
        if (event.getInitiator() == null || !Objects.equals(event.getInitiator().getId(), userId)) {
            throw new NotFoundException("Event " + eventId + " does not belong to user " + userId);
        }
        return event;
    }

    /**
     * Ensures eventDate ≥ now + {hours}.
     */
    private void validateEventDateAtLeast(LocalDateTime eventDate, int hours) {
        if (eventDate == null) throw new BadRequestException("eventDate must not be null");
        if (eventDate.isBefore(LocalDateTime.now().plusHours(hours))) {
            throw new BadRequestException("eventDate must be at least " + hours + " hours from now");
        }
    }

    /**
     * Ensures eventDate is not in the past.
     */
    private void validateEventDateNotPast(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("eventDate must not be in the past");
        }
    }

    /**
     * Parses stateAction string (SEND_TO_REVIEW, etc.) to enum.
     */
    private StateAction parseStateAction(String raw) {
        if (raw == null) return null;
        try {
            return StateAction.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid stateAction: " + raw);
        }
    }

    /**
     * Overload for DTOs already containing enum.
     */
    private StateAction parseStateAction(StateAction raw) {
        return raw;
    }
}