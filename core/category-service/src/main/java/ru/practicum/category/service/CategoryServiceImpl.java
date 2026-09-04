package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dal.model.Category;
import ru.practicum.category.dal.repository.CategoryRepository;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.client.EventClient;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventClient eventClient;

    @Transactional(readOnly = true)
    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);
        return categoryRepository.findAll(page).stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public CategoryDto createCategory(CategoryDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ConflictException("Category name cannot be empty");
        }

        if (categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Category with name '" + dto.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(dto.getName())
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(saved);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto dto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        if (dto.getName() != null && !dto.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(dto.getName())) {
                throw new ConflictException("Category with name '" + dto.getName() + "' already exists");
            }
            category.setName(dto.getName());
        }

        Category updated = categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(updated);
    }

    @Override
    public void deleteCategory(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with id=" + catId + " was not found");
        }

        if (eventClient.checkCategoryInUse(catId)) {
            throw new ConflictException("Category is used by events and cannot be deleted");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public List<CategoryDto> getCategoryByIds(List<Long> catIds) {
        List<Category> allById = categoryRepository.findAllById(catIds);
        List<Long> findedIds = allById.stream().map(Category::getId).distinct().toList();

        HashSet<Long> ids = new HashSet<>(catIds);
        if (!ids.containsAll(findedIds)) {
            findedIds.forEach(ids::remove);
            log.warn("Не удалось найти категории для следующих id:{}", ids);
        }
        return allById.stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }
}
