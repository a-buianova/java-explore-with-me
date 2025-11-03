package ru.practicum.ewm.comments.controller.priv;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentDto;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

/**
 * Private API for managing own comments.
 * User can create, update, delete, and list personal comments.
 */
@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Validated
public class CommentPrivateController {

    private final CommentService service;

    /** Create a new comment under a published event (optionally as a reply). */
    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto add(@PathVariable @Positive Long userId,
                          @PathVariable @Positive Long eventId,
                          @RequestBody @Valid NewCommentDto dto) {
        return service.addComment(userId, eventId, dto);
    }

    /** Update own comment text. */
    @PatchMapping("/{commentId}")
    public CommentDto update(@PathVariable @Positive Long userId,
                             @PathVariable @Positive Long commentId,
                             @RequestBody @Valid UpdateCommentDto dto) {
        return service.updateComment(userId, commentId, dto);
    }

    /** Soft-delete own comment. */
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long userId,
                       @PathVariable @Positive Long commentId) {
        service.deleteComment(userId, commentId);
    }

    /** List user’s visible (non-deleted) comments. */
    @GetMapping
    public List<CommentDto> myComments(@PathVariable @Positive Long userId,
                                       @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                       @RequestParam(defaultValue = "10") @Positive int size) {
        return service.getUserComments(userId, from, size);
    }
}