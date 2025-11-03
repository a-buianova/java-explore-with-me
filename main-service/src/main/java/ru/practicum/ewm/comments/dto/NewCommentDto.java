package ru.practicum.ewm.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for creating a new comment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCommentDto {

    /** Comment text (10–2000 chars). */
    @NotBlank
    @Size(min = 10, max = 2000)
    private String text;

    /** Optional parent comment ID (for replies). */
    private Long parentComment;
}