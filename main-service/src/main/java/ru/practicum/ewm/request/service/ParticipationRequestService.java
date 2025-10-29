package ru.practicum.ewm.request.service;

import ru.practicum.ewm.request.dto.*;

import java.util.List;

/**
 * Service contract for managing participation requests.
 * <p>
 * Includes both user-side operations (create, cancel)
 * and organizer-side operations (manage event requests).
 */
public interface ParticipationRequestService {

    /** Returns all participation requests created by a specific user. */
    List<ParticipationRequestDto> getUserRequests(Long userId);

    /** Creates a new participation request for a published event. */
    ParticipationRequestDto addRequest(Long userId, Long eventId);

    /** Cancels user's own participation request. */
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    /** Retrieves all participation requests for the organizer’s event. */
    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    /** Updates statuses (CONFIRMED / REJECTED) for the event's requests. */
    EventRequestStatusUpdateResult updateRequestStatuses(Long userId,
                                                         Long eventId,
                                                         EventRequestStatusUpdateRequest req);
}