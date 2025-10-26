package ru.practicum.ewm.compilation.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;

import java.util.List;

/** Application service for managing compilations. */
public interface CompilationService {

    /** Creates a new compilation. */
    CompilationDto create(NewCompilationDto req);

    /** Deletes a compilation by ID. */
    void delete(Long compId);

    /** Partially updates a compilation. */
    CompilationDto update(Long compId, UpdateCompilationRequest req);

    /** Returns a paginated list of compilations (optionally filtered by pinned). */
    List<CompilationDto> getAll(Boolean pinned, Pageable pageable);

    /** Returns a compilation by ID. */
    CompilationDto getById(Long compId);
}