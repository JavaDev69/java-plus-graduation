package ru.practicum.analyzer.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 14:46
 * @project java-plus-graduation
 */
public interface UserActionService {
    void handle(UserActionAvro userActionAvro);
}
