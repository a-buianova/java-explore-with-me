package ru.practicum.ewm.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentDto;
import ru.practicum.ewm.comments.mapper.CommentMapper;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.comments.model.CommentState;
import ru.practicum.ewm.comments.repository.CommentRepository;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.common.util.PageUtil;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Comment service implementation with moderation workflow.
 * <p>
 * Handles user comments on events with full moderation lifecycle:
 * <ul>
 *   <li>Users can comment only published events.</li>
 *   <li>New comments are created in {@code PENDING} state and require admin approval.</li>
 *   <li>Admins can approve (publish) or reject comments.</li>
 *   <li>Only published comments are visible publicly.</li>
 *   <li>Authors can edit their published comments within 24 hours after creation.</li>
 * </ul>
 * Includes validation for ownership, parent-child consistency, and time-based editing window.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository repository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentMapper mapper;

    /**
     * Creates a new comment for a published event.
     * The comment starts in {@link CommentState#PENDING} and awaits moderation.
     * Optionally supports replies to other published comments.
     *
     * @param userId  author ID
     * @param eventId target event ID
     * @param dto     text and optional parent comment
     * @return created comment DTO
     */
    @Override
    public CommentDto addComment(long userId, long eventId, NewCommentDto dto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: id=" + eventId));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot comment unpublished event: id=" + eventId);
        }

        Comment comment = Comment.builder()
                .author(author)
                .event(event)
                .text(dto.getText())
                .state(CommentState.PENDING)
                .build();

        if (dto.getParentComment() != null) {
            Comment parent = repository.findById(dto.getParentComment())
                    .orElseThrow(() -> new NotFoundException("Parent comment not found: id=" + dto.getParentComment()));
            if (!parent.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Parent comment belongs to another event");
            }
            if (parent.getState() != CommentState.PUBLISHED) {
                throw new ConflictException("Cannot reply to a non-published parent comment");
            }
            comment.setParentComment(parent);
        }

        return mapper.toDto(repository.save(comment));
    }

    /**
     * Updates text of user's own published comment within 24 hours.
     * Edits are not allowed while pending moderation or after rejection.
     *
     * @param userId    author ID
     * @param commentId comment ID
     * @param dto       new text payload
     * @return updated comment DTO
     */
    @Override
    public CommentDto updateComment(long userId, long commentId, UpdateCommentDto dto) {
        Comment comment = repository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: id=" + commentId));

        if (comment.getAuthor() == null || !comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Cannot edit others' comments");
        }
        if (comment.getState() == CommentState.PENDING) {
            throw new ConflictException("Cannot edit while pending moderation");
        }
        if (comment.getState() != CommentState.PUBLISHED) {
            throw new ConflictException("Only published comments can be edited");
        }
        if (comment.getCreationDate().plusHours(24).isBefore(LocalDateTime.now())) {
            throw new ConflictException("Edit window (24h) has expired");
        }

        comment.setText(dto.getText());
        comment.setEdited(true);
        return mapper.toDto(repository.save(comment));
    }

    /**
     * Deletes user's own comment permanently.
     *
     * @param userId    author ID
     * @param commentId comment ID
     */
    @Override
    public void deleteComment(long userId, long commentId) {
        Comment comment = repository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: id=" + commentId));

        if (comment.getAuthor() == null || !comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Cannot delete others' comments");
        }

        repository.delete(comment);
    }

    /**
     * Returns a single comment by ID.
     * Only published comments are visible publicly.
     *
     * @param id comment ID
     * @return comment DTO
     */
    @Override
    @Transactional(readOnly = true)
    public CommentDto getComment(long id) {
        Comment comment = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found: id=" + id));
        if (comment.getState() != CommentState.PUBLISHED) {
            throw new NotFoundException("Comment not found: id=" + id);
        }
        return mapper.toDto(comment);
    }

    /**
     * Returns all published comments for a given event (public view).
     *
     * @param eventId event ID
     * @param from    offset
     * @param size    limit
     * @return list of published comments
     */
    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByEvent(long eventId, int from, int size) {
        Pageable pageable = PageUtil.byFromSize(from, size, null);
        return repository.findByEvent_IdAndState(eventId, CommentState.PUBLISHED, pageable)
                .map(mapper::toDto)
                .toList();
    }

    /**
     * Returns all comments created by the specified user (any state).
     *
     * @param userId author ID
     * @param from   offset
     * @param size   limit
     * @return list of user's comments
     */
    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getUserComments(long userId, int from, int size) {
        Pageable pageable = PageUtil.byFromSize(from, size, null);
        return repository.findByAuthor_Id(userId, pageable)
                .map(mapper::toDto)
                .toList();
    }

    /**
     * Approves a pending comment (publishes it).
     *
     * @param commentId comment ID
     */
    @Override
    public void approveComment(long commentId) {
        Comment comment = repository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: id=" + commentId));
        if (comment.getState() != CommentState.PENDING) {
            throw new ConflictException("Only pending comments can be approved");
        }
        comment.setState(CommentState.PUBLISHED);
        repository.save(comment);
    }

    /**
     * Rejects a pending comment (marks as hidden).
     *
     * @param commentId comment ID
     */
    @Override
    public void rejectComment(long commentId) {
        Comment comment = repository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: id=" + commentId));
        if (comment.getState() != CommentState.PENDING) {
            throw new ConflictException("Only pending comments can be rejected");
        }
        comment.setState(CommentState.REJECTED);
        repository.save(comment);
    }

    /**
     * Returns moderation queue of pending comments.
     * Used by admins to review comments awaiting approval.
     *
     * @param from offset (≥ 0)
     * @param size number of comments to return (> 0)
     * @return list of pending comments for moderation
     */
    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getPending(int from, int size) {
        Pageable pageable = PageUtil.byFromSize(from, size, null);
        return repository.findByState(CommentState.PENDING, pageable)
                .map(mapper::toDto)
                .toList();
    }
}