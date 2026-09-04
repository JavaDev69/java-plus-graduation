package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.service.CategoryService;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.operations.CategoryOperation;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController implements CategoryOperation {

    private final CategoryService categoryService;

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return categoryService.getCategories(from, size);
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        return categoryService.getCategoryById(catId);
    }

    @Override
    public List<CategoryDto> getCategoriesByIds(List<Long> catIds) {
        return categoryService.getCategoryByIds(catIds);
    }
}
