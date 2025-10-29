package ru.practicum.ewm.common.dto.mapper;

/**
 * Minimal contract for converting between Entity and DTO.
 *
 * @param <E> entity type
 * @param <D> DTO type
 */
public interface Mapper<E, D> {

    /** Converts entity to DTO. */
    D toDto(E entity);

    /** Converts DTO to entity. */
    E toEntity(D dto);
}