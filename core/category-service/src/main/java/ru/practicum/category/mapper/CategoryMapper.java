package ru.practicum.category.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.category.dal.model.Category;
import ru.practicum.dto.categories.CategoryDto;

@UtilityClass
public class CategoryMapper {

    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category toCategory(CategoryDto dto) {
        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}
