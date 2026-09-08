package ru.practicum.serialization.avro;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Andrew Vilkov
 * @created 07.09.2026 - 10:37
 * @project java-plus-graduation
 */
@Slf4j
public class ActionSerializer implements Serializer<SpecificRecordBase> {
    private final EncoderFactory encoderFactory = EncoderFactory.get();

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            return null;
        }

        try (var baos = new ByteArrayOutputStream()) {
            SpecificDatumWriter<Object> writer = new SpecificDatumWriter<>(data.getSchema());
            Encoder encoder = encoderFactory.binaryEncoder(baos, null);
            writer.write(data, encoder);
            encoder.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Ошибка сериализации данных для топика [{}]", topic, e);
            throw new SerializationException("Ошибка сериализации данных для топика [" + topic + "]", e);
        }
    }
}
