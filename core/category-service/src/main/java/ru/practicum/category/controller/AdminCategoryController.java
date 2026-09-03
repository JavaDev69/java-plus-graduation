package ru.practicum.category.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.service.CategoryService;
import ru.practicum.dto.categories.CategoryDto;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto dto) {
        CategoryDto created = categoryService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long catId,
            @Valid @RequestBody CategoryDto dto
    ) {
        CategoryDto updated = categoryService.updateCategory(catId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("Получен запрос на удаление категории ID:{}", catId);
        categoryService.deleteCategory(catId);
    }
}
