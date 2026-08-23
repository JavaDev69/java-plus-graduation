package ru.practicum;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.config.FeignClientConfig;
import ru.practicum.dto.StatsOperation;

@FeignClient(
        name = "stats-server",
        configuration = FeignClientConfig.class)
public interface StatsClient extends StatsOperation {

}
