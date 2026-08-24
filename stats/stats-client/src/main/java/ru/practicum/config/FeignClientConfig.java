package ru.practicum.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * @author Andrew Vilkov
 * @created 19.08.2026 - 16:17
 * @project java-plus-graduation
 */
public class FeignClientConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
}
