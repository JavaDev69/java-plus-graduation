package ru.practicum.category.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.category.dal.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

}
