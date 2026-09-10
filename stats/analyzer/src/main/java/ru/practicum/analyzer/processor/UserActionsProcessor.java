package ru.practicum.analyzer.processor;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.properties.ActionsConsumerProperties;
import ru.practicum.analyzer.service.UserActionService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 14:10
 * @project java-plus-graduation
 */
@Slf4j
@Component
public class UserActionsProcessor extends AbstractProcessor<UserActionAvro> {
    private final UserActionService userActionService;

    public UserActionsProcessor(
            @Qualifier("actionsConsumer") Consumer<String, UserActionAvro> consumer,
            ActionsConsumerProperties consumerProperties,
            UserActionService userActionService) {
        super(consumer, consumerProperties);
        this.userActionService = userActionService;
    }

    @Override
    protected void handleRecord(ConsumerRecord<String, UserActionAvro> record) {
        UserActionAvro actionAvro = record.value();
        log.info("Получено сообщение о действиях пользователей: {}", actionAvro);
        userActionService.handle(actionAvro);
        log.info("Обработано сообщение о действиях пользователей: {}", actionAvro);
    }

    @PreDestroy
    public void shutdown() {
        consumer.wakeup();
    }
}
