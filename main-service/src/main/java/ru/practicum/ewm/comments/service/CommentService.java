package ru.practicum.ewm.comments.service;

import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentDto;

import java.util.List;

/**
 * Comment service interface.
 * Defines public (read), private (self-management), and admin (moderation) operations.
 */
public interface CommentService {

    CommentDto addComment(long userId, long eventId, NewCommentDto dto);

    CommentDto updateComment(long userId, long commentId, UpdateCommentDto dto);

    void deleteComment(long userId, long commentId);

    CommentDto getComment(long id);

    List<CommentDto> getCommentsByEvent(long eventId, int from, int size);

    List<CommentDto> getUserComments(long userId, int from, int size);

    /** Returns moderation queue (pending comments). */
    List<CommentDto> getPending(int from, int size);

    /** Approves a pending comment (publishes it). */
    void approveComment(long commentId);

    /** Rejects a pending comment (marks as hidden). */
    void rejectComment(long commentId);
}