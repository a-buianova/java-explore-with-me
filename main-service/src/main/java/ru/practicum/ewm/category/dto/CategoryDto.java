package ru.practicum.ewm.category.dto;

import lombok.*;

/**
 * Public representation of a category.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long id;
    private String name;
}