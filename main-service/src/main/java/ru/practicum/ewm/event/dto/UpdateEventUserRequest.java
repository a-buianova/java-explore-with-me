package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.ewm.event.model.StateAction;

import java.time.LocalDateTime;

/**
 * Request DTO for partial event updates by the event initiator.
 * Used to edit pending or canceled events.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventUserRequest {

    /** Short annotation (20–2000 chars). */
    @Size(min = 20, max = 2000)
    private String annotation;

    /** Detailed description (20–7000 chars). */
    @Size(min = 20, max = 7000)
    private String description;

    /** Event title (3–120 chars). */
    @Size(min = 3, max = 120)
    private String title;

    /** Updated category ID. */
    private Long category;

    /** Updated location coordinates. */
    private LocationDto location;

    /** Paid flag. */
    private Boolean paid;

    /** Participant limit (0 = unlimited). */
    @PositiveOrZero
    private Integer participantLimit;

    /** Whether moderation of participation requests is required. */
    private Boolean requestModeration;

    /** New event date/time. */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    /** State transition requested by initiator (SEND_TO_REVIEW / CANCEL_REVIEW). */
    private StateAction stateAction;
}