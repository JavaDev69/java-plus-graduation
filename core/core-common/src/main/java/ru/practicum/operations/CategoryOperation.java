package ru.practicum.operations;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.categories.CategoryDto;

import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 27.08.2026 - 14:28
 * @project java-plus-graduation
 */
public interface CategoryOperation {
    @GetMapping
    ResponseEntity<List<CategoryDto>> getCategories(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    );

    @GetMapping("/{catId}")
    ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long catId);

    @GetMapping("/byIds")
    ResponseEntity<List<CategoryDto>> getCategoriesByIds(@RequestParam List<Long> catIds);
}
