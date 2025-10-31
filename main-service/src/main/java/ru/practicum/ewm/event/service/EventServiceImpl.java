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
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.StateAction;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.comments.repository.CommentRepository;
import ru.practicum.ewm.comments.model.CommentState;
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
    private final CommentRepository commentRepository;

    /**
     * Delegates public event search to existing implementation, using a structured DTO instead of individual params.
     */
    @Override
    public Page<EventShortDto> searchPublic(PublicEventSearchRequest req, HttpServletRequest request) {
        Collection<Long> cats = (req.getCategories() == null || req.getCategories().isEmpty())
                ? null : req.getCategories();

        return searchPublic(
                req.getText(),
                cats,
                req.getPaid(),
                req.getRangeStart(),
                req.getRangeEnd(),
                req.isOnlyAvailable(),
                req.getSort(),
                req.getFrom(),
                req.getSize(),
                request
        );
    }


    /**
     * Delegates admin event search to existing logic, using a structured DTO instead of raw parameters.
     */
    @Override
    public Page<EventFullDto> searchAdmin(AdminEventSearchRequest req) {
        Collection<Long> users = (req.getUsers() == null || req.getUsers().isEmpty()) ? null : req.getUsers();
        Collection<String> states = (req.getStates() == null || req.getStates().isEmpty()) ? null : req.getStates();
        Collection<Long> categories = (req.getCategories() == null || req.getCategories().isEmpty())
                ? null : req.getCategories();

        return searchAdmin(
                users,
                states,
                categories,
                req.getRangeStart(),
                req.getRangeEnd(),
                req.getFrom(),
                req.getSize()
        );
    }

    /** Create a new event in PENDING state. Event date must be ≥ 2h from now (409 on violation). */
    @Override
    @Transactional
    public EventFullDto create(long userId, NewEventDto dto) {
        User initiator = getUserOrThrow(userId);
        Category category = getCategoryOrThrow(dto.getCategory());
        validateEventDateAtLeastConflict(dto.getEventDate(), 2);

        Event entity = EventMapper.toEntity(dto, category, initiator);
        entity.setState(EventState.PENDING);
        entity.setCreatedOn(LocalDateTime.now());
        entity.setPublishedOn(null);
        entity.setConfirmedRequests(0);

        Event saved = eventRepository.save(entity);
        log.info("Created event id={} by user={}", saved.getId(), userId);
        return EventMapper.toFullDto(saved, 0L);
    }

    /** Update by initiator. Allowed only for PENDING or CANCELED. (400 on bad date) */
    @Override
    @Transactional
    public EventFullDto updateByInitiator(long userId, long eventId, UpdateEventUserRequest dto) {
        Event event = getOwnedEventOrThrow(userId, eventId);
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be updated by initiator");
        }

        if (dto.getParticipantLimit() != null && dto.getParticipantLimit() < 0) {
            throw new BadRequestException("participantLimit cannot be negative");
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
            validateEventDateAtLeastBadRequest(dto.getEventDate(), 2);
        }

        Event saved = eventRepository.save(event);
        return EventMapper.toFullDto(saved, 0L);
    }

    /** List initiator’s events with offset pagination. */
    @Override
    public Page<EventShortDto> findByInitiator(long userId, int from, int size) {
        getUserOrThrow(userId);
        var pageable = PageUtil.byFromSize(from, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        return eventRepository.findAllByInitiatorId(userId, pageable)
                .map(e -> EventMapper.toShortDto(e, 0L));
    }

    /** Get a single event owned by initiator. */
    @Override
    public EventFullDto getByInitiator(long userId, long eventId) {
        Event event = getOwnedEventOrThrow(userId, eventId);
        return EventMapper.toFullDto(event, 0L);
    }

    /** Public search: filters + pagination + sorting (EVENT_DATE/VIEWS). */
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
        String textParam = (text == null || text.isBlank()) ? "" : text.trim();
        if (sort != null && sort.isBlank()) sort = null;

        safeSendHit(request);

        LocalDateTime start = (rangeStart == null) ? LocalDateTime.now() : rangeStart;
        LocalDateTime end   = (rangeEnd   == null) ? start : rangeEnd;
        if (end.isBefore(start)) {
            throw new BadRequestException("end must be equal to or after start");
        }

        if (categories != null && categories.isEmpty()) {
            categories = null;
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
                sortByViews = true;
            }
        }

        var pageable = PageUtil.byFromSize(from, size, srt);

        LocalDateTime endForDb    = (rangeEnd == null) ? start.plusYears(100) : rangeEnd;
        LocalDateTime endForStats = (rangeEnd != null) ? rangeEnd : LocalDateTime.now(); // NOTE: 1) intervals for stats service are always 'live'

        Page<Event> page = (categories == null)
                ? eventRepository.searchPublicNoCats(textParam, paid, paid == null, start, endForDb, rangeEnd == null, pageable)
                : eventRepository.searchPublicWithCats(textParam, categories, paid, paid == null, start, endForDb, rangeEnd == null, pageable);

        List<Event> content = Optional.ofNullable(page.getContent()).orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (onlyAvailable) {
            content = content.stream()
                    .filter(e -> e.getParticipantLimit() == 0
                            || e.getConfirmedRequests() < e.getParticipantLimit())
                    .collect(Collectors.toList());
        }

        Map<Long, Long> viewsMap = fetchViewsFor(content, start, endForStats);

        if (sortByViews) {
            content.sort(Comparator.comparingLong((Event e) -> viewsMap.getOrDefault(e.getId(), 0L)).reversed());
        }

        List<Long> eventIds = content.stream()
                .map(Event::getId)
                .filter(Objects::nonNull)
                .toList();

        List<Object[]> rows = commentRepository.countByEventIdsAndState(eventIds, CommentState.PUBLISHED);
        Map<Long, Long> commentCounts = new HashMap<>();
        for (Object[] row : rows) {
            commentCounts.put((Long) row[0], (Long) row[1]);
        }

        List<EventShortDto> mapped = content.stream()
                .map(e -> EventMapper.toShortDto(e, viewsMap.getOrDefault(e.getId(), 0L)))
                .toList();

        return new PageImpl<>(mapped, pageable, page.getTotalElements());
    }

    /** Public read: event must be PUBLISHED. Logs a hit and enriches views. */
    @Override
    public EventFullDto getPublicById(long eventId, HttpServletRequest request) {
        safeSendHit(request);
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found or not published"));

        LocalDateTime from = Optional.ofNullable(event.getPublishedOn())
                .orElse(event.getCreatedOn());

        Map<Long, Long> views = fetchViewsFor(List.of(event), from, LocalDateTime.now());
        long commentCount = commentRepository.countByEvent_IdAndState(eventId, CommentState.PUBLISHED);
        return EventMapper.toFullDto(event, views.getOrDefault(eventId, 0L), commentCount);
    }

    /** Admin search with filters and date range validation. */
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

        if (users != null && users.isEmpty()) users = null;
        if (categories != null && categories.isEmpty()) categories = null;

        Collection<EventState> stateEnums = parseStates(states);
        if (stateEnums != null && stateEnums.isEmpty()) stateEnums = null;

        Page<Event> page = eventRepository.searchAdmin(
                users, users == null,
                stateEnums, stateEnums == null,
                categories, categories == null,
                rangeStart, rangeStart == null,
                rangeEnd, rangeEnd == null,
                pageable);
        return page.map(e -> EventMapper.toFullDto(e, 0L));
    }

    /** Admin patch + moderation. Supports PUBLISH_EVENT / REJECT_EVENT. */
    @Override
    @Transactional
    public EventFullDto updateByAdmin(long eventId, UpdateEventAdminRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (dto.getParticipantLimit() != null && dto.getParticipantLimit() < 0) {
            throw new BadRequestException("participantLimit cannot be negative");
        }

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

    // ===== helpers =====

    private void safeSendHit(HttpServletRequest request) {
        if (request == null) return;
        try {
            statsClient.sendHit(request);
        } catch (Throwable ex) {
            log.warn("stats sendHit failed: {}", ex.getMessage());
        }
    }

    /** Safe view aggregation */
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
                    if (vs == null || vs.getUri() == null) continue;
                    Long id = extractId(vs.getUri());
                    if (id == null) continue;
                    long hits = (vs.getHits() == null) ? 0L : vs.getHits();
                    map.merge(id, hits, Long::sum);
                }
            }

            for (String u : uris) {
                Long id = extractId(u);
                if (id != null) map.putIfAbsent(id, 0L);
            }
            return map;
        } catch (Throwable ex) {
            log.warn("stats getStats failed: {}", ex.getMessage());
            Map<Long, Long> fallback = new HashMap<>();
            for (Event e : events) if (e.getId() != null) fallback.put(e.getId(), 0L);
            return fallback;
        }
    }

    private static Long extractId(String uri) {
        if (uri == null) return null;
        String[] p = uri.split("/");
        if (p.length >= 3 && "events".equals(p[1])) {
            try {
                return Long.parseLong(p[2]);
            } catch (NumberFormatException ignored) { }
        }
        return null;
    }

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

    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Category getCategoryOrThrow(long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));
    }

    private Event getOwnedEventOrThrow(long userId, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
        if (event.getInitiator() == null || !Objects.equals(event.getInitiator().getId(), userId)) {
            throw new NotFoundException("Event " + eventId + " does not belong to user " + userId);
        }
        return event;
    }

    private void validateEventDateAtLeastConflict(LocalDateTime eventDate, int hours) {
        if (eventDate == null) {
            throw new ConflictException("Event date must not be null");
        }
        if (eventDate.isBefore(LocalDateTime.now().plusHours(hours))) {
            throw new ConflictException("Event date must be at least " + hours + " hours from now");
        }
    }

    private void validateEventDateAtLeastBadRequest(LocalDateTime eventDate, int hours) {
        if (eventDate == null) {
            throw new BadRequestException("eventDate must not be null");
        }
        if (eventDate.isBefore(LocalDateTime.now().plusHours(hours))) {
            throw new BadRequestException("eventDate must be at least " + hours + " hours from now");
        }
    }

    private void validateEventDateNotPast(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("eventDate must not be in the past");
        }
    }

    private StateAction parseStateAction(String raw) {
        if (raw == null) return null;
        try {
            return StateAction.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid stateAction: " + raw);
        }
    }

    private StateAction parseStateAction(StateAction raw) {
        return raw;
    }
}