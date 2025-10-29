package ru.practicum.ewm.compilation.dto;

import lombok.*;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

/** Read model for compilations exposed via public/admin APIs. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationDto {

    private Long id;
    private String title;
    private boolean pinned;

    /** Events included in this compilation. */
    @Builder.Default
    private List<EventShortDto> events = List.of();
}