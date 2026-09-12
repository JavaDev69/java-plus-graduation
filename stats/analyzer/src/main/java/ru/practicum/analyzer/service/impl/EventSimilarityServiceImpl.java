package ru.practicum.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.dal.model.EventSimilarity;
import ru.practicum.analyzer.dal.repository.SimilarityRepository;
import ru.practicum.analyzer.mapper.EventSimilarityMapper;
import ru.practicum.analyzer.service.EventSimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.Optional;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 14:49
 * @project java-plus-graduation
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class EventSimilarityServiceImpl implements EventSimilarityService {
    private final SimilarityRepository similarityRepository;
    private final EventSimilarityMapper mapper;

    @Override
    public void handle(EventSimilarityAvro similarityAvro) {
        log.info("Начата обработка сообщения о сходстве мероприятий: {}", similarityAvro);
        EventSimilarity entity = mapper.toEntity(similarityAvro);

        Optional<EventSimilarity> event = similarityRepository
                .findOneByFirstEventAndSecondEvent(entity.getFirstEvent(), entity.getSecondEvent());

        event.ifPresentOrElse(e -> {
                    e.setSimilarity(entity.getSimilarity());
                    e.setTimestamp(entity.getTimestamp());
                    EventSimilarity saved = similarityRepository.save(e);
                    log.info("Запись обновлена: {}", saved);
                },
                () -> {
                    EventSimilarity saved = similarityRepository.save(entity);
                    log.info("Запись сохранена: {}", saved);
                }
        );
    }
}