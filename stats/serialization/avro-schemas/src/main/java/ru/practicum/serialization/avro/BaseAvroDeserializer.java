package ru.practicum.serialization.avro;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 12:38
 * @project java-plus-graduation
 */
public class BaseAvroDeserializer <T extends SpecificRecordBase> implements Deserializer<T> {
    private final DecoderFactory decoderFactory;
    private final SpecificDatumReader<T> reader;

    public BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        reader = new SpecificDatumReader<>(schema);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            if (data != null) {
                BinaryDecoder binaryDecoder = decoderFactory.binaryDecoder(data, null);
                return this.reader.read(null, binaryDecoder);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка десериализации данных из топика [" + topic + "]", e);
        }
    }
}
