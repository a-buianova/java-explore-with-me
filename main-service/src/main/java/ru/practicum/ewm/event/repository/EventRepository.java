package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

/**
 * JPA repository for events.
 * Handles both public and admin-level event search with dynamic filters.
 */
public interface EventRepository extends JpaRepository<Event, Long> {

    /** Returns all events created by the given initiator. */
    Page<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    /** Returns a single event by ID if it matches the required state. */
    Optional<Event> findByIdAndState(Long eventId, EventState state);

    /**
     * Public search for published events when categories filter is NOT provided.
     */
    @Query("""
        SELECT e
        FROM Event e
        WHERE e.state = ru.practicum.ewm.event.model.EventState.PUBLISHED
          AND (
                 :text = ''
              OR LOWER(e.annotation)  LIKE CONCAT('%', LOWER(:text), '%')
              OR LOWER(e.description) LIKE CONCAT('%', LOWER(:text), '%')
          )
          AND ( :paidIsNull = true OR e.paid = :paid )
          AND e.eventDate >= :start
          AND ( :endDateIsNull = true OR e.eventDate <= :endDate )
        """)
    Page<Event> searchPublicNoCats(@Param("text") String text,
                                   @Param("paid") Boolean paid,
                                   @Param("paidIsNull") boolean paidIsNull,
                                   @Param("start") LocalDateTime start,
                                   @Param("endDate") LocalDateTime endDate,
                                   @Param("endDateIsNull") boolean endDateIsNull,
                                   Pageable pageable);

    /**
     * Public search for published events when categories filter IS provided.
     */
    @Query("""
        SELECT e
        FROM Event e
        WHERE e.state = ru.practicum.ewm.event.model.EventState.PUBLISHED
          AND (
                 :text = ''
              OR LOWER(e.annotation)  LIKE CONCAT('%', LOWER(:text), '%')
              OR LOWER(e.description) LIKE CONCAT('%', LOWER(:text), '%')
          )
          AND e.category.id IN :categories
          AND ( :paidIsNull = true OR e.paid = :paid )
          AND e.eventDate >= :start
          AND ( :endDateIsNull = true OR e.eventDate <= :endDate )
        """)
    Page<Event> searchPublicWithCats(@Param("text") String text,
                                     @Param("categories") Collection<Long> categories,
                                     @Param("paid") Boolean paid,
                                     @Param("paidIsNull") boolean paidIsNull,
                                     @Param("start") LocalDateTime start,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("endDateIsNull") boolean endDateIsNull,
                                     Pageable pageable);

    /**
     * Admin search: supports filtering by user IDs, event states, categories and date range.
     */
    @Query("""
        SELECT e FROM Event e
        WHERE (:usersIsNull = true OR e.initiator.id IN :users)
          AND (:statesIsNull = true OR e.state IN :states)
          AND (:categoriesIsNull = true OR e.category.id IN :categories)
          AND (:rangeStartIsNull = true OR e.eventDate >= :rangeStart)
          AND (:rangeEndIsNull = true OR e.eventDate <= :rangeEnd)
        """)
    Page<Event> searchAdmin(@Param("users") Collection<Long> users,
                            @Param("usersIsNull") boolean usersIsNull,
                            @Param("states") Collection<EventState> states,
                            @Param("statesIsNull") boolean statesIsNull,
                            @Param("categories") Collection<Long> categories,
                            @Param("categoriesIsNull") boolean categoriesIsNull,
                            @Param("rangeStart") LocalDateTime rangeStart,
                            @Param("rangeStartIsNull") boolean rangeStartIsNull,
                            @Param("rangeEnd") LocalDateTime rangeEnd,
                            @Param("rangeEndIsNull") boolean rangeEndIsNull,
                            Pageable pageable);

    /** Counts how many events belong to the given category. */
    long countByCategoryId(Long categoryId);
}