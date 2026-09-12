package ru.practicum.serialization.avro;

import org.apache.avro.Schema;
import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 12:38
 * @project java-plus-graduation
 */
public class ActionDeserializer extends BaseAvroDeserializer<UserActionAvro>{
    public ActionDeserializer() {
        super(UserActionAvro.getClassSchema());
    }
}
