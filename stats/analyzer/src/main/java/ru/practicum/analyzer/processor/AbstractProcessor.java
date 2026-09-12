package ru.practicum.analyzer.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import ru.practicum.analyzer.properties.ConsumerProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 14:04
 * @project java-plus-graduation
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractProcessor<T extends SpecificRecordBase> implements Runnable {
    protected final Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
    protected final Consumer<String, T> consumer;
    protected final ConsumerProperties consumerProperties;

    @Override
    public void run() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            consumer.subscribe(Collections.singletonList(consumerProperties.getTopic()));

            while (true) {
                ConsumerRecords<String, T> records =
                        consumer.poll(Duration.ofMillis(consumerProperties.getPollTimeoutMs()));

                int count = 0;
                for (ConsumerRecord<String, T> record : records) {
                    handleRecord(record);
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitSync(offsets);
            }
        } catch (WakeupException ignore) {
            // игнорируем - закрываем консьюмер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки сообщения", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }

    protected void manageOffsets(
            ConsumerRecord<String, T> record,
            int count,
            Consumer<String, T> consumer) {
        offsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1));

        if (count % 10 == 0) {
            log.debug("Начата фиксация смещения: {}", record.offset());

            consumer.commitAsync(offsets, (offset, exception) -> {
                if (exception != null) {
                    log.warn("Во время фиксации произошла ошибка. Смещение: {}", offset, exception);
                }
            });
        }
    }

    protected abstract void handleRecord(ConsumerRecord<String, T> record);
}
