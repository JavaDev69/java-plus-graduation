package ru.practicum.aggregator.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.properties.WeightProperties;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Vilkov
 * @created 09.09.2026 - 12:25
 * @project java-plus-graduation
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class EventHandlerImpl implements EventHandler {
    private final WeightProperties weightProperties;
    // Map<Event, Map<User, Weight>> — матрица весов действий пользователей c мероприятиями
    //  ключ — это мероприятие,
    //  значение — ещё одно отображение, где ключ — пользователь,
    //  а значение — максимальный вес из всех его действий с этим мероприятием
    Map<Long, Map<Long, Double>> eventToUserWeight = new HashMap<>();

    // ключ — мероприятие, а значение — сумма весов действий пользователей с ним
    Map<Long, Double> eventToWeight = new HashMap<>();

    // Map<Event, Map<Event, S_min>> — сумма минимальных весов для каждой пары мероприятий
    // ключ - одно из мероприятий,
    // значением — ещё одно отображение, где ключ — второе мероприятие, значение — сумма их минимальных весов
    Map<Long, Map<Long, Double>> minWeightsSum = new HashMap<>();

    @Override
    public List<EventSimilarityAvro> handleRecord(ConsumerRecord<String, UserActionAvro> record) {
        UserActionAvro userAction = record.value();
        log.info("Начата обработка события: {}", userAction);

        double weight = getWeight(userAction.getActionType());
        Double previousWeight = eventToUserWeight
                .computeIfAbsent(userAction.getEventId(), k -> new HashMap<>())
                .getOrDefault(userAction.getUserId(), 0.);

        if (weight <= previousWeight) {
            return Collections.emptyList();
        }

        eventToUserWeight
                .get(userAction.getEventId())
                .put(userAction.getUserId(), weight);
        log.info("Обновлено значение весов действий пользователей для мероприятия({}) с '{}' на '{}' ",
                userAction.getEventId(), previousWeight, weight);

        updateEventToWeight(userAction.getEventId(), weight, previousWeight);

        return updateMinWeightsSum(
                userAction.getEventId(),
                userAction.getUserId(),
                weight,
                previousWeight,
                userAction.getTimestamp());
    }

    private void updateEventToWeight(Long eventId, double weight, double previousWeight) {
        Double current = eventToWeight.getOrDefault(eventId, 0.);
        double newWeight = current + (weight - previousWeight);

        eventToWeight.put(eventId, newWeight);
        log.info("Обновлено значение суммы весов для мероприятия({}) с '{}' на '{}' ",eventId, current, newWeight);
    }

    private List<EventSimilarityAvro> updateMinWeightsSum(long eventId, long userId, double weight, Double previousWeight, Instant timestamp) {
        List<EventSimilarityAvro> result = new ArrayList<>();
        eventToUserWeight.entrySet().stream()
                .filter(e -> e.getValue().containsKey(userId))
                .filter(e -> !e.getKey().equals(eventId))
                .forEach(e -> {
                    long otherEventId = e.getKey();
                    double otherEventWeight = e.getValue().get(userId);

                    double previousMinWeight = Math.min(previousWeight, otherEventWeight);
                    double newMinWeight = Math.min(weight,otherEventWeight);
                    double previousMinWeightsSum = get(eventId,otherEventId);
                    double newMinWeightsSum = previousMinWeightsSum;

                    if (previousMinWeight != newMinWeight) {
                        newMinWeightsSum = previousMinWeightsSum - previousMinWeight + newMinWeight;

                        put(eventId,otherEventId,newMinWeightsSum);

                        log.info("Обновлена сумма минимальных весов для мероприятий ({}, {}) с '{}' на '{}'",
                                eventId, otherEventId, previousMinWeightsSum, newMinWeightsSum);
                    }

                    double similarity = calcSimilarity( eventId, otherEventId, newMinWeightsSum);

                    result.add(getEventSimilarityAvro(eventId, otherEventId, similarity, timestamp));
                });
        return result;
    }

    private EventSimilarityAvro getEventSimilarityAvro(long eventId, long otherEventId, double similarity, Instant timestamp) {
        long firstEventId = Math.min(eventId, otherEventId);
        long secondEventId = Math.max(eventId, otherEventId);

        return EventSimilarityAvro.newBuilder()
                .setEventA(firstEventId)
                .setEventB(secondEventId)
                .setTimestamp(timestamp)
                .setScore(similarity)
                .build();
    }

    private double calcSimilarity(long eventId, long otherEventId, double minWeightsSum) {
        if(minWeightsSum == 0) {
            return 0;
        }

        Double eventWeight = eventToWeight.get(eventId);
        Double otherEventWeight = eventToWeight.get(otherEventId);
        return minWeightsSum / (Math.sqrt(eventWeight) *  Math.sqrt(otherEventWeight));
    }

    public void put(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSum
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    public double get(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSum
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private double getWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> weightProperties.getView();
            case REGISTER -> weightProperties.getRegister();
            case LIKE -> weightProperties.getLike();
            case null -> 0.;
        };
    }
}
