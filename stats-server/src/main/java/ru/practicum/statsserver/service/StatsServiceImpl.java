package ru.practicum.statsserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.statsserver.exception.BadRequestException;
import ru.practicum.statsserver.mapper.StatsMapper;
import ru.practicum.statsserver.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository repository;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto hit) {
        if (hit == null) {
            throw new BadRequestException("hit must not be null");
        }
        // Avoid logging raw IP (PII)
        log.debug("Saving hit: app={}, uri={}, ts={}", hit.getApp(), hit.getUri(), hit.getTimestamp());
        repository.save(StatsMapper.toEntity(hit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start == null || end == null) {
            throw new BadRequestException("start and end must be provided");
        }
        if (end.isBefore(start)) {
            throw new BadRequestException("end must be equal to or after start");
        }

        boolean urisEmpty = (uris == null || uris.isEmpty());
        log.debug("Query stats: start={}, end={}, unique={}, uris={}", start, end, unique, uris);

        return unique
                ? repository.findStatsUnique(start, end, uris, urisEmpty)
                : repository.findStats(start, end, uris, urisEmpty);
    }
}