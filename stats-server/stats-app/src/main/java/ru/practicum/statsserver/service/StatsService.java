package ru.practicum.statsserver.service;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

/** Business API for statistics operations. */
public interface StatsService {
    /** Persist a new endpoint hit. */
    void saveHit(EndpointHitDto hit);

    /** Query aggregated stats (total or unique by IP) for an optional set of URIs. */
    List<ViewStats> getStats(LocalDateTime start,
                             LocalDateTime end,
                             List<String> uris,
                             boolean unique);
}