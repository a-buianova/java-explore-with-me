package ru.practicum.statsserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.statsserver.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for the Statistics Service.
 * Endpoints comply with the OpenAPI specification:
 *  - POST /hit   — record a hit
 *  - GET  /stats — retrieve aggregated statistics
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class StatsController {

    private final StatsService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@Valid @RequestBody EndpointHitDto body) {
        service.saveHit(body);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(
            @RequestParam("start")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam("end")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(value = "uris", required = false) List<String> uris,
            @RequestParam(value = "unique", defaultValue = "false") boolean unique
    ) {
        return service.getStats(start, end, uris, unique);
    }
}