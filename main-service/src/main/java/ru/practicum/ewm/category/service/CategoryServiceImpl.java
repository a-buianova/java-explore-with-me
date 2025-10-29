package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.common.util.PageUtil;
import ru.practicum.ewm.event.repository.EventRepository;

import java.util.List;

/**
 * Implementation of {@link CategoryService}.
 * Ensures unique names, prevents deletion of used categories, supports pagination.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    /**
     * Creates a new category.
     * @param dto request payload with name
     * @return created category
     * @throws ConflictException if category name already exists
     */
    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto dto) {
        Category saved = categoryRepository.save(CategoryMapper.toEntity(dto));
        log.info("Created category id={}, name='{}'", saved.getId(), saved.getName());
        return CategoryMapper.toDto(saved);
    }

    /**
     * Deletes category by ID.
     * @param catId ID of category to delete
     * @throws NotFoundException if category does not exist
     * @throws ConflictException if category has linked events
     */
    @Override
    @Transactional
    public void delete(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        long count = eventRepository.countByCategoryId(catId);
        if (count > 0) {
            throw new ConflictException("Cannot delete category with existing events");
        }

        categoryRepository.delete(category);
        log.info("Deleted category id={}", catId);
    }

    /**
     * Updates category name.
     * @param catId category ID
     * @param dto payload with new name
     * @return updated category DTO
     * @throws NotFoundException if category not found
     * @throws ConflictException if new name already used by another category
     */
    @Override
    @Transactional
    public CategoryDto update(Long catId, NewCategoryDto dto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        category.setName(dto.getName());
        Category updated = categoryRepository.save(category);
        log.info("Updated category id={} name='{}'", updated.getId(), updated.getName());
        return CategoryMapper.toDto(updated);
    }

    /**
     * Returns all categories (paginated).
     * @param from offset
     * @param size limit
     * @return list of categories
     */
    @Override
    public List<CategoryDto> getAll(int from, int size) {
        var pageable = PageUtil.byFromSize(from, size, Sort.by("id").ascending());
        return categoryRepository.findAll(pageable)
                .map(CategoryMapper::toDto)
                .toList();
    }

    /**
     * Returns a single category by ID.
     * @param catId category ID
     * @return found category
     * @throws NotFoundException if category not found
     */
    @Override
    public CategoryDto getById(Long catId) {
        return categoryRepository.findById(catId)
                .map(CategoryMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }
}