package ru.practicum.ewm.comments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO representing a comment returned in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {

    /** Unique comment ID. */
    private Long id;

    /** Comment text. */
    private String text;

    /** Author name. */
    private String author;

    /** Related event ID. */
    private Long eventId;

    /** Parent comment ID (if this is a reply). */
    private Long parentComment;

    /** Creation timestamp. */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creationDate;

    /** Last update timestamp (null if never edited). */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;

    /** Whether the comment was edited after creation. */
    private boolean edited;
}