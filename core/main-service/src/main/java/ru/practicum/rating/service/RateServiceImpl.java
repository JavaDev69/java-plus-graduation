package ru.practicum.rating.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.UserClient;
import ru.practicum.dto.events.EventState;
import ru.practicum.dto.user.UserDto;
import ru.practicum.events.Event;
import ru.practicum.events.EventsRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.rating.Rate;
import ru.practicum.rating.RateRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RateServiceImpl implements RateService {

    private final RateRepository rateRepository;
    private final EventsRepository eventsRepository;
    private final UserClient userClient;

    @Override
    public void addRate(Long userId, Long eventId, boolean isLike) {
        log.info("Пользователь ID={} ставит {} событию ID={}", userId, isLike ? "ЛАЙК" : "ДИЗЛАЙК", eventId);

        UserDto user = userClient.getById(userId);

        Event event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя оценивать неопубликованные события");
        }

        if (event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Инициатор не может оценивать собственное событие");
        }

        // Ищем существующую оценку. Если есть — обновляем, если нет — создаем.
        Rate rate = rateRepository.findByEventIdAndUserId(eventId, userId)
                .orElse(Rate.builder()
                        .userId(user.getId())
                        .event(event)
                        .build());

        rate.setIsLike(isLike);
        rateRepository.save(rate);
    }

    @Override
    public void deleteRate(Long userId, Long eventId) {
        log.info("Пользователь ID={} удаляет оценку у события ID={}", userId, eventId);

        // Проверяем, существует ли пользователь и событие
        UserDto user = userClient.getById(userId);

        if (!eventsRepository.existsById(eventId)) {
            throw new NotFoundException("Событие с ID " + eventId + " не найдено");
        }

        Rate rate = rateRepository.findByEventIdAndUserId(eventId, user.getId())
                .orElseThrow(() -> new NotFoundException("Оценка пользователя ID=" + userId + " для события ID=" + eventId + " не найдена"));

        rateRepository.delete(rate);
    }
}