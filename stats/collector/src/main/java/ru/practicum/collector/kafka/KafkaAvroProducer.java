package ru.practicum.collector.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.collector.properties.KafkaProperties;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;

/**
 * @author Andrew Vilkov
 * @created 07.09.2026 - 15:14
 * @project java-plus-graduation
 */
@Component
@RequiredArgsConstructor
public class KafkaAvroProducer implements AutoCloseable {
    private final Producer<String, Object> producer;
    private final KafkaProperties kafkaProperties;

    public void send(UserActionAvro userAction) {
        ProducerRecord<String, Object> record =
                new ProducerRecord<>(
                        kafkaProperties.getTopic(),
                        null,
                        userAction.getTimestamp().toEpochMilli(),
                        null,
                        userAction
                );
        producer.send(record);
    }

    @Override
    public void close() throws Exception {
        producer.flush();
        producer.close(Duration.ofSeconds(10));
    }
}
