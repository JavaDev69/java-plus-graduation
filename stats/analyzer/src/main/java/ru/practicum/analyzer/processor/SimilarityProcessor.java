package ru.practicum.analyzer.processor;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.properties.SimilarityConsumerProperties;
import ru.practicum.analyzer.service.EventSimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 14:04
 * @project java-plus-graduation
 */
@Slf4j
@Component
public class SimilarityProcessor extends AbstractProcessor<EventSimilarityAvro> {
    private final EventSimilarityService similarityService;

    public SimilarityProcessor(
            @Qualifier("similarityConsumer") Consumer<String, EventSimilarityAvro> consumer,
            SimilarityConsumerProperties consumerProperties,
            EventSimilarityService similarityService) {
        super(consumer, consumerProperties);
        this.similarityService = similarityService;
    }

    @Override
    protected void handleRecord(ConsumerRecord<String, EventSimilarityAvro> record) {
        EventSimilarityAvro similarityAvro = record.value();
        log.info("Получено сообщение о сходстве мероприятий: {}", similarityAvro);
        similarityService.handle(similarityAvro);
        log.info("Обработано сообщение о сходстве мероприятий: {}", similarityAvro);
    }

    @PreDestroy
    public void shutdown() {
        consumer.wakeup();
    }
}
