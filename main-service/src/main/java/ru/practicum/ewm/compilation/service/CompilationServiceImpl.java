package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Use cases for compilations: create, update, list, read.
 * Ensures referenced events exist when setting a custom list.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    /** Creates a compilation. Events list is optional. */
    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto req) {
        Set<Event> events = loadEvents(req.getEvents());
        Compilation entity = CompilationMapper.toEntity(req, events);
        entity = compilationRepository.save(entity);
        log.info("Created compilation id={} pinned={} title='{}'",
                entity.getId(), entity.getPinned(), entity.getTitle());
        return toDtoWithEvents(entity);
    }

    /** Deletes compilation by id. */
    @Override
    @Transactional
    public void delete(Long compId) {
        Compilation entity = getOrThrow(compId);
        compilationRepository.delete(entity);
        log.info("Deleted compilation id={}", compId);
    }

    /**
     * Partially updates a compilation:
     * title, pinned and the full set of events (if provided).
     */
    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest req) {
        Compilation entity = getOrThrow(compId);

        if (req.getTitle() != null) {
            entity.setTitle(req.getTitle());
        }
        if (req.getPinned() != null) {
            entity.setPinned(req.getPinned());
        }
        if (req.getEvents() != null) {
            entity.setEvents(loadEvents(req.getEvents()));
        }

        Compilation saved = compilationRepository.save(entity);
        log.info("Updated compilation id={} pinned={} title='{}'",
                saved.getId(), saved.getPinned(), saved.getTitle());
        return toDtoWithEvents(saved);
    }

    /** Returns compilations page converted to DTOs. */
    @Override
    public List<CompilationDto> getAll(Boolean pinned, Pageable pageable) {
        var page = (pinned == null)
                ? compilationRepository.findAll(pageable)
                : compilationRepository.findAllByPinned(pinned, pageable);
        return page.getContent().stream().map(this::toDtoWithEvents).toList();
    }

    /** Returns a single compilation by id. */
    @Override
    public CompilationDto getById(Long compId) {
        Compilation c = getOrThrow(compId);
        return toDtoWithEvents(c);
    }

    // ---------------- helpers ----------------

    private Compilation getOrThrow(Long id) {
        return compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Compilation %d not found".formatted(id)));
    }

    /**
     * Loads events by ids; if any are missing — throws 404 with a clear list.
     * Сохраняем порядок в точности как пришёл в запросе.
     */
    private Set<Event> loadEvents(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Set.of();

        List<Event> found = eventRepository.findAllById(ids);
        Map<Long, Event> byId = found.stream()
                .collect(Collectors.toMap(Event::getId, e -> e));

        List<Long> missing = ids.stream()
                .filter(id -> !byId.containsKey(id))
                .distinct()
                .sorted()
                .toList();
        if (!missing.isEmpty()) {
            throw new NotFoundException("Events not found: " + missing);
        }

        LinkedHashSet<Event> ordered = new LinkedHashSet<>();
        for (Long id : ids) {
            ordered.add(byId.get(id));
        }
        return ordered;
    }

    /** Maps entity + its events to DTO. Views are not required here (0). */
    private CompilationDto toDtoWithEvents(Compilation entity) {
        Set<Event> eventsSet = (entity.getEvents() == null) ? Set.of() : entity.getEvents();

        List<EventShortDto> events = eventsSet.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Event::getId))
                .map(e -> EventMapper.toShortDto(e, 0L))
                .toList();

        return CompilationMapper.toDto(entity, events);
    }
}