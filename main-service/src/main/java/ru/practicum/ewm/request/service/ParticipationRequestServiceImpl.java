package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.common.exception.BadRequestException;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.dto.*;
import ru.practicum.ewm.request.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Use cases for participation requests: create/cancel/list/update statuses.
 * Enforces business constraints and participant limit.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository repository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    /** Returns current user's requests to others' events. */
    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return repository.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    /**
     * Creates a new participation request.
     * - 409 if initiator requests their own event
     * - 409 if event not published
     * - 409 if participant limit reached
     * - Auto CONFIRMED if (limit==0 OR moderation==false); otherwise PENDING
     */
    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (repository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }
        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }
        if (!"PUBLISHED".equals(event.getState().name())) {
            throw new ConflictException("Cannot participate in unpublished event");
        }
        if (event.getParticipantLimit() < 0) {
            throw new BadRequestException("Participant limit cannot be negative");
        }
        // ВАЖНО: если лимит > 0 и он уже достигнут — запрещаем создавать запрос вовсе (409)
        if (event.getParticipantLimit() > 0 &&
                event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Event participant limit reached");
        }

        boolean autoConfirm = (event.getParticipantLimit() == 0) || !event.isRequestModeration();
        RequestStatus status = autoConfirm ? RequestStatus.CONFIRMED : RequestStatus.PENDING;

        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(user)
                .status(status)
                .created(LocalDateTime.now())
                .build();

        ParticipationRequest saved = repository.save(request);

        // Если автоподтверждение — увеличиваем счётчик подтверждённых.
        if (status == RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        return ParticipationRequestMapper.toDto(saved);
    }

    /** Cancels user's own request (status → CANCELED). */
    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = repository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        if (!Objects.equals(request.getRequester().getId(), userId)) {
            throw new ConflictException("Cannot cancel someone else's request");
        }
        request.setStatus(RequestStatus.CANCELED);
        return ParticipationRequestMapper.toDto(repository.save(request));
    }

    /** Returns requests for organizer's own event. */
    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("User is not the initiator of this event");
        }
        return repository.findAllByEventId(eventId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    /**
     * Batch updates request statuses by organizer (CONFIRMED/REJECTED).
     * - 409 if trying to confirm when limit is already reached.
     */
    @Override
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId,
                                                                EventRequestStatusUpdateRequest req) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("User is not the initiator of this event");
        }

        List<ParticipationRequest> requests = repository.findAllById(req.getRequestIds());
        if (requests.size() != req.getRequestIds().size()) {
            throw new NotFoundException("Some requests not found");
        }

        List<ParticipationRequest> confirmed = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();

        for (ParticipationRequest r : requests) {
            if (r.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Only pending requests can be changed");
            }

            if ("CONFIRMED".equalsIgnoreCase(req.getStatus())) {
                // Жёстко соблюдаем лимит: если мест нет — 409 (а не молчаливый REJECTED)
                if (event.getParticipantLimit() > 0 &&
                        event.getConfirmedRequests() >= event.getParticipantLimit()) {
                    throw new ConflictException("Participant limit reached");
                }
                r.setStatus(RequestStatus.CONFIRMED);
                confirmed.add(r);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);

            } else if ("REJECTED".equalsIgnoreCase(req.getStatus())) {
                r.setStatus(RequestStatus.REJECTED);
                rejected.add(r);
            }
        }

        repository.saveAll(requests);
        eventRepository.save(event);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed.stream().map(ParticipationRequestMapper::toDto).toList())
                .rejectedRequests(rejected.stream().map(ParticipationRequestMapper::toDto).toList())
                .build();
    }
}