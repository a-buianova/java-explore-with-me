package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

/** Partial update of a compilation (admin API). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompilationRequest {

    /** New title (≤ 50 characters). */
    @Size(max = 50)
    private String title;

    /** Whether the compilation is pinned. */
    private Boolean pinned;

    /** New list of event IDs to replace existing ones. */
    private List<Long> events;
}