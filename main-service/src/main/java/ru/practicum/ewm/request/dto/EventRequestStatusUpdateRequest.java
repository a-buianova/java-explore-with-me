package ru.practicum.ewm.request.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

/**
 * Request object for bulk status update by the event organizer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestStatusUpdateRequest {

    /** IDs of requests to update. */
    @NotEmpty
    private List<Long> requestIds;

    /** Target status: CONFIRMED or REJECTED. */
    private String status;
}