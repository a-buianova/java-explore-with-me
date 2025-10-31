package ru.practicum.ewm.comments.controller.admin;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

/**
 * Admin API for comment moderation.
 * Provides moderation queue and approve/reject operations.
 */
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
public class CommentAdminController {

    private final CommentService service;

    /** Returns moderation queue (PENDING comments). */
    @GetMapping
    public List<CommentDto> queue(@RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                  @RequestParam(defaultValue = "10") @Positive int size) {
        return service.getPending(from, size);
    }

    /** Approves a pending comment (publishes it). */
    @PatchMapping("/{commentId}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approve(@PathVariable @Positive Long commentId) {
        service.approveComment(commentId);
    }

    /** Rejects a pending comment (hides it). */
    @PatchMapping("/{commentId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@PathVariable @Positive Long commentId) {
        service.rejectComment(commentId);
    }
}