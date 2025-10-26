package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import java.time.LocalDateTime;
import java.util.*;

/** JPA repository for events. */
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndState(Long eventId, EventState state);

    @Query("""
        SELECT e FROM Event e
        WHERE e.state = 'PUBLISHED'
          AND (:text IS NULL OR (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))
                                 OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))))
          AND (:categories IS NULL OR e.category.id IN :categories)
          AND (:paid IS NULL OR e.paid = :paid)
          AND (e.eventDate >= :start)
          AND (:end IS NULL OR e.eventDate <= :end)
        """)
    Page<Event> searchPublic(@Param("text") String text,
                             @Param("categories") Collection<Long> categories,
                             @Param("paid") Boolean paid,
                             @Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end,
                             Pageable pageable);

    @Query("""
        SELECT e FROM Event e
        WHERE (:users IS NULL OR e.initiator.id IN :users)
          AND (:states IS NULL OR e.state IN :states)
          AND (:categories IS NULL OR e.category.id IN :categories)
          AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart)
          AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
        """)
    Page<Event> searchAdmin(@Param("users") Collection<Long> users,
                            @Param("states") Collection<EventState> states,
                            @Param("categories") Collection<Long> categories,
                            @Param("rangeStart") LocalDateTime rangeStart,
                            @Param("rangeEnd") LocalDateTime rangeEnd,
                            Pageable pageable);

    long countByCategoryId(Long categoryId);
}