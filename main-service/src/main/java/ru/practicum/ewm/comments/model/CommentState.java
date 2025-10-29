package ru.practicum.ewm.comments.model;

/**
 * Moderation state of a comment.
 * Defines visibility and moderation workflow.
 */
public enum CommentState {
    PENDING,
    PUBLISHED,
    REJECTED
}