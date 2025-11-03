package ru.practicum.ewm.comments.controller.pub;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

/**
 * Public read-only API for event comments.
 * Returns only comments that passed moderation (state = PUBLISHED).
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class CommentPublicController {

    private final CommentService service;

    /** Returns published comments for an event. */
    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getEventComments(@PathVariable @Positive Long eventId,
                                             @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                             @RequestParam(defaultValue = "10") @Positive int size) {
        return service.getCommentsByEvent(eventId, from, size);
    }

    /** Returns a single published comment by ID. */
    @GetMapping("/comments/{commentId}")
    public CommentDto getComment(@PathVariable @Positive Long commentId) {
        return service.getComment(commentId);
    }
}