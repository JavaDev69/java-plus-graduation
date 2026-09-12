package ru.practicum.aggregator.handler;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 09.09.2026 - 11:52
 * @project java-plus-graduation
 */
public interface EventHandler {
    List<EventSimilarityAvro> handleRecord(ConsumerRecord<String, UserActionAvro> record);

}
