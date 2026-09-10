package ru.practicum.analyzer.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 14:42
 * @project java-plus-graduation
 */
public interface EventSimilarityService {
    void handle(EventSimilarityAvro similarityAvro);
}
