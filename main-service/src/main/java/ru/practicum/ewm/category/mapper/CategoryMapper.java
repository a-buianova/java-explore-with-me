package ru.practicum.ewm.category.mapper;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;

/** Converts between Category entities and DTOs. */
public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryDto toDto(Category entity) {
        if (entity == null) return null;
        return CategoryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public static Category toEntity(NewCategoryDto dto) {
        if (dto == null) return null;
        return Category.builder()
                .name(dto.getName())
                .build();
    }
}