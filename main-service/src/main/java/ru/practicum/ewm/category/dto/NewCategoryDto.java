package ru.practicum.ewm.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/** DTO used for category creation and update requests. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCategoryDto {

    @NotBlank(message = "Category name must not be blank")
    @Size(min = 1, max = 50, message = "Category name length must be between 1 and 50")
    private String name;
}