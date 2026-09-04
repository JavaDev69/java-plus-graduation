package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.client.config.FeignClientConfig;
import ru.practicum.operations.CategoryOperation;

/**
 * @author Andrew Vilkov
 * @created 25.08.2026 - 15:27
 * @project java-plus-graduation
 */
@FeignClient(
        name = "category-service",
        path = "/categories",
        configuration = FeignClientConfig.class)
public interface CategoryClient extends CategoryOperation {
}
