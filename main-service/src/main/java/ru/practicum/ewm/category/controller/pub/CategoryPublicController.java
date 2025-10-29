package ru.practicum.ewm.category.controller.public_;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import java.util.List;

/**
 * Public API for reading categories.
 * Provides paginated list and single category retrieval.
 */
@Validated
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryPublicController {

    private final CategoryService service;

    /** Returns a paginated list of categories. */
    @GetMapping
    public List<CategoryDto> getAll(@RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                    @RequestParam(defaultValue = "10") @Positive int size) {
        return service.getAll(from, size);
    }

    /** Returns a category by its ID. */
    @GetMapping("/{catId}")
    public CategoryDto getById(@PathVariable Long catId) {
        return service.getById(catId);
    }
}