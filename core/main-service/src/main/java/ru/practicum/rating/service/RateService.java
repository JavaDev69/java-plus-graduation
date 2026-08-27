package ru.practicum.rating.service;

public interface RateService {
    void addRate(Long userId, Long eventId, boolean isLike);

    void deleteRate(Long userId, Long eventId);
}