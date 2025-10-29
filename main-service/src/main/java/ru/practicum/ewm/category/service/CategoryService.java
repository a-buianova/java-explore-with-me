package ru.practicum.ewm.category.service;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;

import java.util.List;

/**
 * Application service for managing categories.
 */
public interface CategoryService {

    /** Creates a new category. */
    CategoryDto create(NewCategoryDto dto);

    /** Updates an existing category. */
    CategoryDto update(Long catId, NewCategoryDto dto);

    /** Deletes a category if it has no linked events. */
    void delete(Long catId);

    /** Returns all categories (paginated). */
    List<CategoryDto> getAll(int from, int size);

    /** Returns a category by ID. */
    CategoryDto getById(Long catId);
}