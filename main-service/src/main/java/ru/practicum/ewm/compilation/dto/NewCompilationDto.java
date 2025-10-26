package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

/** Request payload for creating a new compilation (admin API). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCompilationDto {

    /** Compilation title (required, ≤ 50 characters). */
    @NotBlank
    @Size(max = 50)
    private String title;

    /** Whether the compilation is pinned on the main page. */
    @Builder.Default
    private Boolean pinned = false;

    /** List of event IDs to include (optional). */
    @Builder.Default
    private List<Long> events = List.of();
}