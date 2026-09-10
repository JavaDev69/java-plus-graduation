package ru.practicum.aggregator.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.aggregator.properties.ConsumerProperties;
import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * @author Andrew Vilkov
 * @created 09.09.2026 - 10:33
 * @project java-plus-graduation
 */
@Configuration
public class KafkaConfig {
    @Bean
    public Consumer<String, UserActionAvro> consumer(ConsumerProperties properties) {
        return new KafkaConsumer<>(properties.getProperties());
    }
}
