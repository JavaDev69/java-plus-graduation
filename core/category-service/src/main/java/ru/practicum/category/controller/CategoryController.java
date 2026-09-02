package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.service.CategoryService;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.operations.CategoryOperation;

import java.util.List;

@Validated
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController implements CategoryOperation {

    private final CategoryService categoryService;

    @Override
    public ResponseEntity<List<CategoryDto>> getCategories(Integer from,Integer size) {
        List<CategoryDto> categories = categoryService.getCategories(from, size);
        return ResponseEntity.ok(categories);
    }

    @Override
    public ResponseEntity<CategoryDto> getCategoryById(Long catId) {
        CategoryDto category = categoryService.getCategoryById(catId);
        return ResponseEntity.ok(category);
    }

    @Override
    public ResponseEntity<List<CategoryDto>> getCategoriesByIds(List<Long> catIds) {
        List<CategoryDto> categories = categoryService.getCategoryByIds(catIds);

        return ResponseEntity.ok(categories);
    }
}
