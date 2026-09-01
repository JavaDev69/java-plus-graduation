package ru.practicum.rating.service;

import java.util.List;
import java.util.Map;

public interface RateService {
    void addRate(Long userId, Long eventId, boolean isLike);

    void deleteRate(Long userId, Long eventId);

    Long getRatingByEventId(Long id);

    Map<Long, Long> getRatingByEventIds(List<Long> eventIds);
}