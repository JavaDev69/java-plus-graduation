package ru.practicum.client.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * @author Andrew Vilkov
 * @created 25.08.2026 - 15:31
 * @project java-plus-graduation
 */
public class FeignClientConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
}
