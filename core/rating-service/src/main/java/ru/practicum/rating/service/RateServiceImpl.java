package ru.practicum.rating.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.EventState;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.rating.dal.model.Rate;
import ru.practicum.rating.dal.repository.RateRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RateServiceImpl implements RateService {

    private final RateRepository rateRepository;
    private final EventClient eventClient;
    private final UserClient userClient;

    @Transactional
    @Override
    public void addRate(Long userId, Long eventId, boolean isLike) {
        log.info("Пользователь ID={} ставит {} событию ID={}", userId, isLike ? "ЛАЙК" : "ДИЗЛАЙК", eventId);

        UserDto user = userClient.getById(userId);

        ResponseEntity<EventFullDto> eventById = eventClient.getEventById(eventId);
        if (eventById.getBody() == null) {
            throw new NotFoundException("Событие с ID " + eventId + " не найдено");
        }

        EventFullDto event = eventById.getBody();

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя оценивать неопубликованные события");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор не может оценивать собственное событие");
        }

        // Ищем существующую оценку. Если есть — обновляем, если нет — создаем.
        Rate rate = rateRepository.findByEventIdAndUserId(eventId, userId)
                .orElse(Rate.builder()
                        .userId(user.getId())
                        .eventId(event.getId())
                        .build());

        rate.setIsLike(isLike);
        rateRepository.save(rate);
    }

    @Transactional
    @Override
    public void deleteRate(Long userId, Long eventId) {
        log.info("Пользователь ID={} удаляет оценку у события ID={}", userId, eventId);

        // Проверяем, существует ли пользователь и событие
        UserDto user = userClient.getById(userId);

        ResponseEntity<EventFullDto> eventById = eventClient.getEventById(eventId);
        if (eventById.getBody() == null) {
            throw new NotFoundException("Событие с ID " + eventId + " не найдено");
        }

        Rate rate = rateRepository.findByEventIdAndUserId(eventId, user.getId())
                .orElseThrow(() -> new NotFoundException("Оценка пользователя ID=" + userId + " для события ID=" + eventId + " не найдена"));

        rateRepository.delete(rate);
    }

    @Override
    public Long getRatingByEventId(Long eventId) {
        log.info("Запрос рейтинга для события с id '{}'", eventId);
        return rateRepository.getRatingForEvent(eventId);
    }

    @Override
    public Map<Long, Long> getRatingByEventIds(List<Long> eventIds) {
        log.info("Запрос рейтинга для списка событий: {}", eventIds);
        return rateRepository.getRatingsForEvents(eventIds).stream()
                .map(e -> Map.entry(e.getEventId(), e.getRating()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}