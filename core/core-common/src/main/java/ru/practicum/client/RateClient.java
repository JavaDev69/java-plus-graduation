package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.client.config.FeignClientConfig;
import ru.practicum.operations.EventOperation;
import ru.practicum.operations.RateOperation;

/**
 * @author Andrew Vilkov
 * @created 25.08.2026 - 15:27
 * @project java-plus-graduation
 */
@FeignClient(
        name = "rating-service",
        path = "/rate",
        configuration = FeignClientConfig.class)
public interface RateClient extends RateOperation {
}
