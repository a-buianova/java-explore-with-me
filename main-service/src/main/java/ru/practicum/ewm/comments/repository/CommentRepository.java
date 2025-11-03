package ru.practicum.ewm.comments.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.comments.model.CommentState;

import java.util.Collection;
import java.util.List;

/**
 * JPA repository for comment entities.
 * Provides filtered pagination for event-, author-based lookups and moderation queue.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** Returns comments by event and moderation state. */
    Page<Comment> findByEvent_IdAndState(Long eventId, CommentState state, Pageable pageable);

    /** Returns the moderation queue by state (e.g., PENDING). */
    Page<Comment> findByState(CommentState state, Pageable pageable);

    /** Returns comments by author (any state). */
    Page<Comment> findByAuthor_Id(Long authorId, Pageable pageable);

    /** Counts comments for an event by moderation state. */
    long countByEvent_IdAndState(Long eventId, CommentState state);

    /** Batch count by event ids and state (to avoid N+1). */
    @Query("""
           SELECT c.event.id, COUNT(c)
           FROM Comment c
           WHERE c.state = :state AND c.event.id IN :eventIds
           GROUP BY c.event.id
           """)
    List<Object[]> countByEventIdsAndState(@Param("eventIds") Collection<Long> eventIds,
                                           @Param("state") CommentState state);
}