package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.request.model.ParticipationRequest;

import java.util.List;

/**
 * Repository for accessing participation request data.
 */
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    /** Finds all requests created by the specified user. */
    List<ParticipationRequest> findAllByRequesterId(Long userId);

    /** Finds all requests for a given event. */
    List<ParticipationRequest> findAllByEventId(Long eventId);

    /** Checks whether a user already has a request for this event. */
    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);
}