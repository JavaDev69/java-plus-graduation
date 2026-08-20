package ru.practicum;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.config.FeignClientConfig;
import ru.practicum.dto.StatsOperation;

@FeignClient(
        name = "stats-service",
        configuration = FeignClientConfig.class)
public interface StatsClient extends StatsOperation {

}
