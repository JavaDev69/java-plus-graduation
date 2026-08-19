package ru.practicum;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.config.FeignClientConfig;
import ru.practicum.dto.StatsOperation;

@FeignClient(
        name = "stats-service",
        url = "${stats.server.url}",
        configuration = FeignClientConfig.class)
public interface StatsClient extends StatsOperation {

}
