package ru.practicum.statsserver.mapper;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.statsserver.model.EndpointHit;

/** Simple DTO ↔ Entity mapper for statistics hits. */
public final class StatsMapper {

    private StatsMapper() {
    }

    public static EndpointHit toEntity(EndpointHitDto dto) {
        if (dto == null) {
            return null;
        }
        return EndpointHit.builder()
                .id(dto.getId())
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public static EndpointHitDto toDto(EndpointHit entity) {
        if (entity == null) {
            return null;
        }
        return EndpointHitDto.builder()
                .id(entity.getId())
                .app(entity.getApp())
                .uri(entity.getUri())
                .ip(entity.getIp())
                .timestamp(entity.getTimestamp())
                .build();
    }
}