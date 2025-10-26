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
 * Business logic for managing participation requests.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository repository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return repository.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (repository.existsByEventIdAndRequesterId(eventId, userId))
            throw new ConflictException("Request already exists");

        if (event.getInitiator().getId().equals(userId))
            throw new ConflictException("Initiator cannot request participation in own event");

        if (!"PUBLISHED".equals(event.getState().name()))
            throw new ConflictException("Cannot participate in unpublished event");

        if (event.getParticipantLimit() < 0)
            throw new BadRequestException("Participant limit cannot be negative");

        if (event.getParticipantLimit() > 0 &&
                event.getConfirmedRequests() >= event.getParticipantLimit())
            throw new ConflictException("Event participant limit reached");

        RequestStatus status = (event.getParticipantLimit() == 0 || !event.isRequestModeration())
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;

        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(user)
                .status(status)
                .created(LocalDateTime.now())
                .build();

        return ParticipationRequestMapper.toDto(repository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = repository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        if (!Objects.equals(request.getRequester().getId(), userId))
            throw new ConflictException("Cannot cancel someone else's request");
        request.setStatus(RequestStatus.CANCELED);
        return ParticipationRequestMapper.toDto(repository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!Objects.equals(event.getInitiator().getId(), userId))
            throw new ConflictException("User is not the initiator of this event");
        return repository.findAllByEventId(eventId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId,
                                                                EventRequestStatusUpdateRequest req) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!Objects.equals(event.getInitiator().getId(), userId))
            throw new ConflictException("User is not the initiator of this event");

        List<ParticipationRequest> requests = repository.findAllById(req.getRequestIds());
        if (requests.size() != req.getRequestIds().size())
            throw new NotFoundException("Some requests not found");

        List<ParticipationRequest> confirmed = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();

        for (ParticipationRequest r : requests) {
            if (r.getStatus() != RequestStatus.PENDING)
                throw new ConflictException("Only pending requests can be changed");

            if ("CONFIRMED".equalsIgnoreCase(req.getStatus())) {
                if (event.getParticipantLimit() > 0 &&
                        event.getConfirmedRequests() >= event.getParticipantLimit()) {
                    r.setStatus(RequestStatus.REJECTED);
                    rejected.add(r);
                } else {
                    r.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(r);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                }
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