package ru.practicum.analyzer.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.analyzer.properties.ActionsConsumerProperties;
import ru.practicum.analyzer.properties.SimilarityConsumerProperties;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * @author Andrew Vilkov
 * @created 09.09.2026 - 10:33
 * @project java-plus-graduation
 */
@Configuration
public class KafkaConfig {
    @Bean(name = "similarityConsumer")
    public Consumer<String, EventSimilarityAvro> similarityConsumer(SimilarityConsumerProperties properties) {
        return new KafkaConsumer<>(properties.getProperties());
    }
    @Bean(name = "actionsConsumer")
    public Consumer<String, UserActionAvro> actionsConsumer(ActionsConsumerProperties properties) {
        return new KafkaConsumer<>(properties.getProperties());
    }
}
