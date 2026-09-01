package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.client.config.FeignClientConfig;
import ru.practicum.operations.EventOperation;
import ru.practicum.operations.RequestOperation;

/**
 * @author Andrew Vilkov
 * @created 25.08.2026 - 15:27
 * @project java-plus-graduation
 */
@FeignClient(
        name = "request-service",
        path = "/users/{userId}/events/{eventId}/requests",
        configuration = FeignClientConfig.class)
public interface RequestClient extends RequestOperation {
}
