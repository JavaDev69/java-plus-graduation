package ru.practicum.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.dal.model.EventSimilarity;
import ru.practicum.analyzer.dal.model.Interaction;
import ru.practicum.analyzer.dal.repository.InteractionRepository;
import ru.practicum.analyzer.mapper.InteractionMapper;
import ru.practicum.analyzer.service.UserActionService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

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
public class UserActionServiceImpl implements UserActionService {
    private final InteractionRepository interactionRepository;
    private final InteractionMapper mapper;

    @Override
    public void handle(UserActionAvro userActionAvro) {
        log.info("Начата обработка сообщения о действиях пользователя с мероприятиями: {}", userActionAvro);
        Interaction entity = mapper.toEntity(userActionAvro);

        Optional<Interaction> interaction = interactionRepository
                .findOneByUserIdAndEventId(entity.getUserId(), entity.getEventId());

        interaction.ifPresentOrElse(e -> {
                    if (e.getRating() >= entity.getRating()) {
                        log.info("Запись не обновлена, новый рейтинг({}) ниже предыдущего({})",
                                entity.getRating(),e.getRating());
                        return;
                    }
                    e.setRating(entity.getRating());
                    e.setTimestamp(entity.getTimestamp());
                    Interaction saved = interactionRepository.save(e);
                    log.info("Запись обновлена: {}", saved);
                },
                () -> {
                    Interaction saved = interactionRepository.save(entity);
                    log.info("Запись сохранена: {}", saved);
                }
        );
    }
}
