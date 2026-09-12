package ru.practicum.serialization.avro;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 12:38
 * @project java-plus-graduation
 */
public class SimilarityDeserializer extends BaseAvroDeserializer<UserActionAvro>{
    public SimilarityDeserializer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}
