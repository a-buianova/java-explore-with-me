package ru.practicum.ewm.request.controller.private_;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.ParticipationRequestService;

import java.util.List;

/**
 * Private API for event organizers:
 * allows managing participation requests for their own events.
 */
@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
public class EventRequestPrivateController {

    private final ParticipationRequestService service;

    /** Returns all participation requests for the organizer’s event. */
    @GetMapping
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId,
                                                          @PathVariable Long eventId) {
        return service.getEventRequests(userId, eventId);
    }

    /** Updates statuses (confirm/reject) of multiple participation requests. */
    @PatchMapping
    public EventRequestStatusUpdateResult updateStatuses(@PathVariable Long userId,
                                                         @PathVariable Long eventId,
                                                         @Valid @RequestBody EventRequestStatusUpdateRequest req) {
        return service.updateRequestStatuses(userId, eventId, req);
    }
}