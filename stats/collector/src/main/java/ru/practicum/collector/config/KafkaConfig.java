package ru.practicum.collector.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import ru.practicum.collector.properties.KafkaProperties;

/**
 * @author Andrew Vilkov
 * @created 07.09.2026 - 10:56
 * @project java-plus-graduation
 */
@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    public Producer<String, Object> producerConfig(KafkaProperties properties) {
        log.info("Loaded KafkaProperties: {}", properties);
        return new KafkaProducer<>(properties.getProperties());
    }
}
