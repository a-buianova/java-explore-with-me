package ru.practicum.statsserver.mapper;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.statsserver.model.EndpointHit;

/** Simple DTO ↔ Entity mapper for statistics hits. */
public final class StatsMapper {
    private StatsMapper() {
    }

    public static EndpointHit toEntity(EndpointHitDto dto) {
        if (dto == null) return null;
        return EndpointHit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }
}