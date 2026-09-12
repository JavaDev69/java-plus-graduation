package ru.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.handler.EventHandler;
import ru.practicum.aggregator.kafka.KafkaEventSimilarityProducer;
import ru.practicum.aggregator.properties.ConsumerProperties;
import ru.practicum.aggregator.properties.ProducerProperties;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Vilkov
 * @created 09.09.2026 - 10:29
 * @project java-plus-graduation
 */
@Slf4j
@RequiredArgsConstructor
@Value
@Component
public class AggregationStarter {
    ProducerProperties producerProperties;
    ConsumerProperties consumerProperties;
    KafkaEventSimilarityProducer producer;
    Consumer<String, UserActionAvro> consumer;
    EventHandler eventHandler;
    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

    public void start() {
        try (producer) {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            consumer.subscribe(Collections.singletonList(consumerProperties.getTopic()));

            while (true) {
                ConsumerRecords<String, UserActionAvro> records =
                        consumer.poll(Duration.ofMillis(consumerProperties.getPollTimeoutMs()));

                int count = 0;
                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    log.debug("Получено событие: {}", record.value());
                    calculateSimilarity(record);
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private void calculateSimilarity(ConsumerRecord<String, UserActionAvro> record) {
        eventHandler
                .handleRecord(record)
                .forEach(
                        similarity -> {
                            log.debug("Есть изменение, запись similarity в топик: {}", similarity);
                            ProducerRecord<String, EventSimilarityAvro> pr = producerRecordFromSimilarity(similarity);
                            producer.send(pr);
                            log.debug("Similarity отправлен.");
                        });
    }

    private ProducerRecord<String, EventSimilarityAvro> producerRecordFromSimilarity(EventSimilarityAvro similarity) {
        String key = String.join("-", String.valueOf(similarity.getEventA()), String.valueOf(similarity.getEventB()));
        return new ProducerRecord<>(
                producerProperties.getTopic(),
                null,
                similarity.getTimestamp().toEpochMilli(),
                key,
                similarity);
    }

    private void manageOffsets(
            ConsumerRecord<String, UserActionAvro> record,
            int count,
            Consumer<String, UserActionAvro> consumer) {
        offsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            log.debug("Начата фиксация смещения: {}", record.offset());

            consumer.commitAsync(
                    offsets,
                    (offset, exception) -> {
                        if (exception != null) {
                            log.warn("Во время фиксации произошла ошибка. Смещение: {}", offset, exception);
                        }
                    }
            );
        }
    }
}
