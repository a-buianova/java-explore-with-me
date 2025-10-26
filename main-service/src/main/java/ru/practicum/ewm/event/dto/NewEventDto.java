package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/** Payload for creating a new event. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewEventDto {

    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;

    @NotNull
    private Long category;

    @NotNull
    private LocationDto location;

    @NotNull
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    /** Whether participation is paid (default: false). */
    @Builder.Default
    private boolean paid = false;

    /** Participant limit (default: 0 — unlimited). */
    @PositiveOrZero
    @Builder.Default
    private int participantLimit = 0;

    /** Whether moderation of requests is required (default: true). */
    @Builder.Default
    private boolean requestModeration = true;
}