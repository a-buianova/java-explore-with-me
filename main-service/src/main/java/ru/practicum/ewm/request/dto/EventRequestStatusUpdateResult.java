package ru.practicum.ewm.request.dto;

import lombok.*;

import java.util.List;

/**
 * Response object containing results of bulk status update.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestStatusUpdateResult {

    /** Successfully confirmed requests. */
    private List<ParticipationRequestDto> confirmedRequests;

    /** Requests that were rejected. */
    private List<ParticipationRequestDto> rejectedRequests;
}