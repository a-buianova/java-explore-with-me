package ru.practicum.stats.client;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

/** Thin HTTP client for the statistics service. */
public interface StatsClient {
    void sendHit(HttpServletRequest request);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}