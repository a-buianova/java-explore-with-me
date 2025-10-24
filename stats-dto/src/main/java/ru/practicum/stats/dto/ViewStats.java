package ru.practicum.stats.dto;

import lombok.*;

/** Aggregated statistics for an endpoint (app + uri + hits). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewStats {
    private String app;
    private String uri;
    private Long hits;
}