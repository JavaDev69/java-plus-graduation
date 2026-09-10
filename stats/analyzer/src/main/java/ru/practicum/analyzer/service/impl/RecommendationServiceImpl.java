package ru.practicum.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.internal.util.compare.ComparableComparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.dal.model.EventSimilarity;
import ru.practicum.analyzer.dal.model.Interaction;
import ru.practicum.analyzer.dal.repository.InteractionRepository;
import ru.practicum.analyzer.dal.repository.SimilarityRepository;
import ru.practicum.analyzer.service.RecommendationService;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingDouble;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 18:45
 * @project java-plus-graduation
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {
    private final InteractionRepository interactionRepository;
    private final SimilarityRepository similarityRepository;

    @Override
    public Stream<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        log.info("getRecommendationsForUser: {}", request.getUserId());

        List<Interaction> userInteractions =
                interactionRepository.findAllByUserIdOrderByTimestampDesc(request.getUserId());

        if (userInteractions.isEmpty()) {
            return Stream.empty();
        }

        Set<Long> userEventIds = userInteractions.stream()
                .map(Interaction::getEventId)
                .collect(Collectors.toSet());

        Map<Long, Double> eventRatings = userInteractions.stream()
                .collect(Collectors.toMap(
                        Interaction::getEventId,
                        Interaction::getRating,
                        Math::max
                ));

        List<EventSimilarity> similarities = similarityRepository.findAllByFirstEventOrSecondEventIn(userEventIds);

        Map<Long, Double> candidateToScore = new HashMap<>();

        for (EventSimilarity similarity : similarities) {
            Long candidateId = userEventIds.contains(similarity.getFirstEvent()) ?
                    similarity.getSecondEvent() : similarity.getFirstEvent();

            Long userEventId = similarity.getFirstEvent().equals(candidateId) ?
                    similarity.getSecondEvent() : similarity.getFirstEvent();

            Double rating = eventRatings.get(userEventId);
            double score = rating * similarity.getSimilarity();
            candidateToScore.merge(candidateId, score, Math::max);
        }

        return candidateToScore.entrySet().stream()
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .limit(request.getMaxResults())
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build());
    }

    @Override
    public Stream<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
       log.info("getSimilarEvents: {}", request.getEventId());

        return similarityRepository
                .findAllByEventIdOrderBySimilarityDesc(request.getEventId(), request.getMaxResults())
                .stream()
                .map(similarity -> {
                    Long eventId = similarity.getFirstEvent().equals(request.getEventId()) ?
                            similarity.getSecondEvent() : similarity.getFirstEvent();
                    return RecommendedEventProto.newBuilder()
                            .setEventId(eventId)
                            .setScore(similarity.getSimilarity())
                            .build();
                });
    }

    @Override
    public Stream<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        log.info("getInteractionsCount: {}", request.getEventIdList());

        Set<Long> eventIds = new HashSet<>(request.getEventIdList());

        return interactionRepository
                .streamAllByEventIdIn(eventIds)
                .collect(groupingBy(Interaction::getEventId, summingDouble(Interaction::getRating)))
                .entrySet().stream()
                .map(e-> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build()
                );
    }
}
