package ru.practicum.collector.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.collector.kafka.KafkaAvroProducer;
import ru.practicum.collector.mapper.UserActionMapper;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;

/**
 * @author Andrew Vilkov
 * @created 07.09.2026 - 15:13
 * @project java-plus-graduation
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserActionService {
    private final KafkaAvroProducer producer;
    private final UserActionMapper mapper;

    public void collectUserAction(@Valid UserActionProto userAction) {
        log.info("Sent user action to Kafka: {}", userAction);
        UserActionAvro action = mapper.toAvro(userAction);
        producer.send(action);
    }
}
