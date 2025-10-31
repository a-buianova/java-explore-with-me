package ru.practicum.ewm.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for updating existing comment text.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCommentDto {

    /** New comment text (10–2000 chars). */
    @NotBlank
    @Size(min = 10, max = 2000)
    private String text;
}