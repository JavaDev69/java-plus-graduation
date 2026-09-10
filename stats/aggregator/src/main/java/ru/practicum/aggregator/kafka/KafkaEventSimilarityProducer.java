package ru.practicum.aggregator.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.properties.ProducerProperties;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.concurrent.Future;

/**
 * @author Andrew Vilkov
 * @created 09.09.2026 - 10:36
 * @project java-plus-graduation
 */
@Component
public class KafkaEventSimilarityProducer implements AutoCloseable{
    private final Producer<String, EventSimilarityAvro> producer;
    private static final int TIMEOUT_SECONDS = 10;

    public KafkaEventSimilarityProducer(ProducerProperties properties) {
        this.producer = new KafkaProducer<>(properties.getProperties());
    }

    public Future<RecordMetadata> send(ProducerRecord<String, EventSimilarityAvro> pr) {
        return producer.send(pr);
    }

    public void flush(){
        producer.flush();
    }

    @Override
    public void close() {
        producer.flush();
        producer.close(Duration.ofSeconds(TIMEOUT_SECONDS));
    }
}
