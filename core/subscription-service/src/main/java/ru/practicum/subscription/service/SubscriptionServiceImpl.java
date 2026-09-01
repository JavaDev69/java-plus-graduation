package ru.practicum.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.events.EventShortDto;
import ru.practicum.dto.events.EventState;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.subscription.dal.model.Subscription;
import ru.practicum.subscription.dal.repository.SubscriptionRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestClient requestClient;

    @Override
    public void subscribe(Long userId, Long publisherId) {
        log.info("Пользователь с ID {} подписывается на пользователя с ID {}", userId, publisherId);

        if (userId.equals(publisherId)) {
            throw new ConflictException("User cannot subscribe to himself");
        }

        UserDto subscriber = userClient.getById(userId);
        UserDto publisher = userClient.getById(publisherId);

        if (subscriptionRepository.existsBySubscriberIdAndPublisherId(userId, publisherId)) {
            throw new ConflictException("User with id=" + userId + " is already subscribed to user with id=" + publisherId);
        }

        subscriptionRepository.save(new Subscription(null, subscriber.getId(), publisher.getId(), LocalDateTime.now(ZoneId.systemDefault())));
        log.info("Пользователь с ID {} успешно подписался на пользователя с ID {}", userId, publisherId);
    }

    @Override
    public void unsubscribe(Long userId, Long publisherId) {
        log.info("Пользователь с ID {} отписывается от пользователя с ID {}", userId, publisherId);

        UserDto subscriber = userClient.getById(userId);
        UserDto publisher = userClient.getById(publisherId);

        int deleted = subscriptionRepository.deleteBySubscriberIdAndPublisherId(subscriber.getId(), publisher.getId());
        if (deleted == 0) {
            throw new NotFoundException("Subscription from user with id=" + userId +
                    " to user with id=" + publisherId + " was not found");
        }

        log.info("Пользователь с ID {} успешно отписался от пользователя с ID {}", userId, publisherId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getActualEventsFromSubscriptions(Long userId, int from, int size) {
        log.info("Получение актуальных событий пользователя с ID {}, from: {}, size: {}", userId, from, size);

        UserDto user = userClient.getById(userId);
        List<Long> publisherIds = subscriptionRepository.findAllPublisherBySubscriberId(user.getId());
        return eventClient.getActualPublishedEventsBySubscriberId(
                user.getId(),
                publisherIds,
                EventState.PUBLISHED,
                LocalDateTime.now(ZoneId.systemDefault()),
                PageRequest.of(from / size, size)
        );
    }

    private Map<Long, Long> getConfirmedRequests(List<EventShortDto> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> eventIds = events.stream()
                .map(EventShortDto::getId)
                .toList();

        return requestClient.countRequestsByEventIdsAndStatus(eventIds, EventState.CONFIRMED);
    }
}
