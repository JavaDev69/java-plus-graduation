package ru.practicum.events.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.client.CategoryClient;
import ru.practicum.client.RateClient;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.ViewStats;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.EventShortDto;
import ru.practicum.dto.events.EventState;
import ru.practicum.dto.events.NewEventDto;
import ru.practicum.dto.events.StateAction;
import ru.practicum.dto.events.UpdateEventAdminRequest;
import ru.practicum.dto.events.UpdateEventRequest;
import ru.practicum.dto.events.UpdateEventUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.events.dal.model.Event;
import ru.practicum.events.dal.model.EventsSortType;
import ru.practicum.events.dal.repository.EventsRepository;
import ru.practicum.events.mapper.EventsMapper;
import ru.practicum.events.moderation.ModerationComment;
import ru.practicum.events.moderation.ModerationCommentRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.EventCreationRuleException;
import ru.practicum.exception.ForbiddenActionException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static ru.practicum.events.mapper.EventsMapper.toEvent;
import static ru.practicum.events.mapper.EventsMapper.toEventFullDto;
import static ru.practicum.events.mapper.EventsMapper.toShortEventDto;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventsServiceImpl implements EventsService {
    private static final int MIN_HOURS_BEFORE_EVENT = 2;
    private final EventsRepository eventRepository;
    private final CategoryClient categoryClient;
    private final RequestClient requestClient;
    private final UserClient userClient;
    private final StatsClient statsClient;
    private final EntityManager entityManager;
    private final RateClient rateClient;
    private final ModerationCommentRepository moderationCommentRepository;

    @Override
    public EventFullDto saveEvent(NewEventDto newEventDto, Long userId) {
        validateEventDate(newEventDto.getEventDate());
        UserDto user = userClient.getById(userId);
        CategoryDto category = categoryClient.getCategoryById(newEventDto.getCategory());

        Event event = toEvent(newEventDto, user, category.getId());
        Event savedEvent = eventRepository.save(event);
        savedEvent.setInitiatorId(user.getId());
        savedEvent.setCategoryId(category.getId());

        return toEventFullDto(savedEvent, category, user, 0L);
    }

    @Override
    public List<EventShortDto> getPublishedEvents(
            String text, List<Long> categoryIds, Boolean paid,
            LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Boolean onlyAvailable, EventsSortType sort, int from, int size) {

        Pageable pageable = PageRequest.of(from / size, size);
        if (rangeStart == null) rangeStart = LocalDateTime.now(ZoneId.systemDefault());

        Specification<Event> spec = Specification.where(EventSpecification.hasStatePublished())
                .and(EventSpecification.hasTextInAnnotationOrDescription(text))
                .and(EventSpecification.belongsToCategories(categoryIds))
                .and(EventSpecification.isPaid(paid))
                .and(EventSpecification.isWithinRange(rangeStart, rangeEnd));

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        if (events.isEmpty()) {
            return emptyList();
        }
        if (Boolean.TRUE.equals(onlyAvailable)) {
            events.removeIf(event -> event.getParticipantLimit() > 0 &&
                    event.getConfirmedRequests() >= event.getParticipantLimit());
        }

        List<Long> userIds = events.stream().map(Event::getInitiatorId).distinct().toList();
        List<UserDto> userDtos = userClient.get(userIds, 0, userIds.size());
        Map<Long, UserDto> users = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));

        Map<Long, Long> requestCounts = getRequestCounts(events.stream().map(Event::getId).toList());
        Map<Long, Long> ratingsMap = getRatingsMap(events);

        Map<Long, CategoryDto> idToCategory = getIdToCategoryForEvent(events);

        List<EventShortDto> dtoList = events.stream()
                .map(event ->
                        toShortEventDto(
                                event,
                                idToCategory.get(event.getCategoryId()),
                                users.get(event.getInitiatorId()),
                                requestCounts.getOrDefault(event.getId(), 0L),
                                ratingsMap.getOrDefault(event.getId(), 0L))
                )
                .collect(Collectors.toList());

        log.info("Список событий до сортировки: {}", dtoList);
        if (sort == EventsSortType.VIEWS) {
            dtoList.sort((e1, e2) ->
                    Long.compare(e2.getViews(), e1.getViews()));
        } else if (sort == EventsSortType.RATING) {
            dtoList.sort((e1, e2) ->
                    Long.compare(e2.getRating(), e1.getRating()));
        }
        log.info("Список событий после сортировки: {}", dtoList);

        return dtoList;
    }

    @Override
    public EventFullDto getPublishedEventById(Long id) {
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        Map<Long, Long> eventToRequest =
                requestClient.countRequestsByEventIdsAndStatus(singletonList(event.getId()), EventState.CONFIRMED);

        event.setConfirmedRequests(eventToRequest.getOrDefault(event.getId(), 0L));
        setViewsToEvents(List.of(event));
        Long rating = rateClient.getRatingByEventId(id);
        UserDto user = userClient.getById(event.getInitiatorId());
        CategoryDto category = event.getCategoryId() == null ? null : categoryClient.getCategoryById(event.getCategoryId());
        return toEventFullDto(event, category, user, rating);
    }

    @Override
    public List<EventShortDto> getShortEventByIds(List<Long> ids) {
        List<Event> allById = eventRepository.findAllById(ids);
        setViewsToEvents(allById);

        Map<Long, Long> eventToRequest = requestClient.countRequestsByEventIdsAndStatus(ids, EventState.CONFIRMED);
        Map<Long, Long> ratingByEventIds = rateClient.getRatingByEventIds(ids);
        Map<Long, CategoryDto> idToCategory = getIdToCategoryForEvent(allById);

        List<Long> userIds = allById.stream().map(Event::getInitiatorId).distinct().toList();
        List<UserDto> userDtos = userClient.get(userIds, 0, userIds.size());
        Map<Long, UserDto> users = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));

        return allById.stream()
                .map(event -> toShortEventDto(
                        event,
                        event.getCategoryId() == null ? null : idToCategory.get(event.getCategoryId()),
                        users.get(event.getInitiatorId()),
                        eventToRequest.getOrDefault(event.getId(), 0L),
                        ratingByEventIds.getOrDefault(event.getId(), 0L))
                )
                .toList();
    }

    @Override
    public List<EventFullDto> getEvents(
            List<Long> userIds,
            List<String> states,
            List<Long> categoryIds,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        if (userIds != null && !userIds.isEmpty()) {
            predicates.add(root.get(Event.Fields.initiatorId).in(userIds));
        }

        if (states != null && !states.isEmpty()) {
            List<EventState> stateEnums = states.stream()
                    .map(EventState::valueOf)
                    .toList();
            predicates.add(root.get(Event.Fields.state).in(stateEnums));
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            predicates.add(root.get(Event.Fields.categoryId).in(categoryIds));
        }

        if (rangeStart != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(Event.Fields.eventDate), rangeStart));
        }
        if (rangeEnd != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(Event.Fields.eventDate), rangeEnd));
        }

        query.select(root).where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get(Event.Fields.eventDate)));

        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = entityManager.createQuery(query)
                .setFirstResult((int) pageRequest.getOffset())
                .setMaxResults(pageRequest.getPageSize())
                .getResultList();

        Map<Long, Long> confirmedRequests = getConfirmedRequestsMap(events);
        Map<Long, Long> ratings = getRatingsMap(events);
        setViewsToEvents(events);

        Map<Long, CategoryDto> idToCategory = getIdToCategoryForEvent(events);

        List<Long> userFromEventsIds = events.stream().map(Event::getInitiatorId).distinct().toList();
        List<UserDto> userDtos = userClient.get(userFromEventsIds, 0, userFromEventsIds.size());
        Map<Long, UserDto> users = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));

        return events.stream()
                .peek(event -> event.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L)))
                .map(event -> toEventFullDto(
                        event,
                        event.getCategoryId() == null ? null : idToCategory.get(event.getCategoryId()),
                        users.get(event.getInitiatorId()),
                        ratings.getOrDefault(event.getId(), 0L))
                )
                .toList();
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        ModerationComment moderationComment = null;

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT -> {
                    if (!event.getState().equals(EventState.PENDING)) {
                        throw new ConflictException("Cannot publish event in state: " + event.getState());
                    }
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                        throw new ConflictException("Event must be at least 1 hour after current time to be published");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> {
                    if (event.getState().equals(EventState.PUBLISHED)) {
                        throw new ConflictException("Cannot reject published event");
                    }

                    if (request.getModerationComment() != null && !request.getModerationComment().trim().isEmpty()) {
                        moderationComment = ModerationComment.builder()
                                .event(event)
                                .commentText(request.getModerationComment())
                                .createdOn(LocalDateTime.now())
                                .build();
                        moderationComment = moderationCommentRepository.save(moderationComment);
                    }

                    event.setState(EventState.CANCELED);
                    event.setRequestModeration(false);
                }
            }
        }

        applyNonNullUpdates(event, request);

        Event saved = eventRepository.save(event);

        Map<Long, Long> eventToRequest =
                requestClient.countRequestsByEventIdsAndStatus(singletonList(saved.getId()), EventState.CONFIRMED);

        saved.setConfirmedRequests(eventToRequest.getOrDefault(saved.getId(), 0L));
        setViewsToEvent(saved);

        Long rating = rateClient.getRatingByEventId(saved.getId());
        CategoryDto category = categoryClient.getCategoryById(saved.getCategoryId());
        UserDto user = userClient.getById(saved.getInitiatorId());

        return EventsMapper.toEventFullDto(
                saved,
                category,
                user,
                moderationComment,
                rating);
    }

    @Override
    @Transactional
    public EventFullDto updateInactiveEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        log.info("Начало обновления события с ID: {} для пользователя с ID: {}", eventId, userId);
        log.debug("Dto {}", updateEventUserRequest);

        // 1. Находим событие по ID
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с ID " + eventId + " не найдено"));

        // 2. Проверяем принадлежность события пользователю
        Long initiatorId = event.getInitiatorId();
        if (!initiatorId.equals(userId)) {
            throw new ForbiddenActionException("Пользователь с ID " + userId + " не является инициатором события " + eventId);
        }

        // 3. Проверяем статус события
        EventState currentState = event.getState();
        if (!currentState.equals(EventState.CANCELED) && !currentState.equals(EventState.PENDING)) {
            throw new ConflictException(
                    "Только отменённые события или события в состоянии ожидания модерации могут быть изменены. Текущий статус: " + currentState
            );
        }

        // 4. Обрабатываем stateAction, если указан
        StateAction stateAction = updateEventUserRequest.getStateAction();
        if (stateAction != null) {
            switch (stateAction) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new ConflictException(
                            "Недопустимое значение stateAction: " + stateAction +
                                    ". Допустимые значения: SEND_TO_REVIEW, CANCEL_REVIEW"
                    );
            }
        }

        // 5. Применяем обновления полей (только не‑null)
        applyNonNullUpdates(event, updateEventUserRequest);

        // 6. Валидируем дату события
        LocalDateTime updateDate = updateEventUserRequest.getEventDate();
        if (updateDate != null) {
            validateEventDate(updateDate);
        } else if (stateAction == StateAction.SEND_TO_REVIEW) {
            validateEventDate(event.getEventDate());
            event.setRequestModeration(true);
        }

        // 7. Сохраняем и возвращаем результат
        Event updatedEvent = eventRepository.save(event);
        log.info("Событие с ID: {} успешно обновлено", eventId);

        Long rating = rateClient.getRatingByEventId(updatedEvent.getId());
        UserDto user = userClient.getById(updatedEvent.getInitiatorId());
        CategoryDto category = categoryClient.getCategoryById(updatedEvent.getCategoryId());

        return toEventFullDto(
                updatedEvent,
                category,
                user,
                rating);
    }

    @Override
    public List<EventFullDto> getUserEvents(Long userId, int from, int size) {
        log.debug("Начинаем поиск событий для пользователя с ID: {}, from: {}, size: {}", userId, from, size);


        UserDto user = userClient.getById(userId);
        List<Event> events = eventRepository.findAllByInitiatorIdWithOffset(user.getId(), from, size);

        if (events.isEmpty()) {
            log.debug("Для пользователя с ID {} не найдено событий", userId);
            return Collections.emptyList();
        }

        Map<Long, Long> confirmedRequests = getConfirmedRequestsMap(events);
        Map<Long, Long> ratings = getRatingsMap(events);
        Map<Long, CategoryDto> idToCategory = getIdToCategoryForEvent(events);

        List<EventFullDto> eventFullDtos = events.stream()
                .peek(event -> event.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L)))
                .map(event -> toEventFullDto(
                        event,
                        event.getCategoryId() == null ? null : idToCategory.get(event.getCategoryId()),
                        user,
                        ratings.getOrDefault(event.getId(), 0L))
                )
                .toList();

        log.info("Найдено {} событий для пользователя с ID {}", events.size(), userId);
        return eventFullDtos;
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        log.debug("Начинаем поиск события с ID: {} для пользователя с ID: {}", eventId, userId);

        // Находим пользователя — если не найден, будет выброшено исключение NotFoundException
        UserDto user = userClient.getById(userId);

        // Ищем событие по ID и проверяем принадлежность пользователю
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiatorId().equals(user.getId())) {
            throw new ForbiddenActionException(
                    "Пользователь с ID " + userId + " не является инициатором события " + eventId
            );
        }

        log.debug("Событие найдено в БД: ID {}, заголовок '{}'", event.getId(), event.getTitle());

        // Получаем количество подтверждённых заявок
        long confirmedRequests = requestClient
                .countRequestsByEventIdsAndStatus(singletonList(event.getId()), EventState.CONFIRMED)
                .getOrDefault(event.getId(), 0L);
        event.setConfirmedRequests(confirmedRequests);

        // Обновляем просмотры
        setViewsToEvent(event);
        Long rating = rateClient.getRatingByEventId(eventId);
        CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
        log.info("Полные данные события подготовлены для возврата");
        return toEventFullDto(event, category, user, rating);
    }

    // --- Приватные вспомогательные методы ---

    private void validateEventDate(LocalDateTime eventDate) {
        LocalDateTime minEventDate = LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT);
        if (eventDate.isBefore(minEventDate)) {
            throw new EventCreationRuleException("eventDate", eventDate, "Событие не удовлетворяет правилам создания");
        }
    }

    private void setViewsToEvent(Event event) {
        List<ViewStats> stats = getStats(List.of("/events/" + event.getId()));
        event.setViews(stats.stream().findFirst().map(ViewStats::getHits).orElse(0L));
    }

    private List<ViewStats> getStats(List<String> uris) {
        try {
            return statsClient
                    .getStats(
                            LocalDateTime.of(2000, 1, 1, 0, 0, 0),
                            LocalDateTime.now(),
                            uris,
                            true
                    );
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Event> events) {
        if (events.isEmpty()) return emptyMap();
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        return requestClient.countRequestsByEventIdsAndStatus(eventIds, EventState.CONFIRMED);
    }

    private void setViewsToEvents(List<Event> events) {
        if (events.isEmpty()) return;
        List<String> uris = events.stream().map(e -> "/events/" + e.getId()).toList();
        List<ViewStats> stats = getStats(uris);
        Map<String, Long> viewsMap = stats.stream().collect(Collectors.toMap(ViewStats::getUri, ViewStats::getHits));
        events.forEach(e -> e.setViews(viewsMap.getOrDefault("/events/" + e.getId(), 0L)));
    }

    private Map<Long, Long> getRequestCounts(List<Long> eventIds) {
        Map<Long, Long> requestIdToCount = requestClient.countRequestsByEventIdsAndStatus(eventIds, EventState.CONFIRMED);
        log.info("Получен список requestIdToCount: {}", requestIdToCount);
        return requestIdToCount;
    }

    private Map<Long, Long> getRatingsMap(List<Event> events) {
        if (events.isEmpty()) return emptyMap();
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> eventIdToRating = rateClient.getRatingByEventIds(eventIds);
        log.info("Получен список eventIdToRating: {}", eventIdToRating);
        return eventIdToRating;
    }

    private <T extends UpdateEventRequest> void applyNonNullUpdates(Event event, T request) {
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getLocation() != null) {
            event.setLocationLat(request.getLocation().getLat());
            event.setLocationLon(request.getLocation().getLon());
        }
        if (request.getCategory() != null) {
            CategoryDto category = categoryClient.getCategoryById(request.getCategory());

            if (category == null) {
                throw new NotFoundException("Category with id=" + request.getCategory() + " was not found");
            }
            event.setCategoryId(category.getId());
        }
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
    }

    @Override
    public List<EventFullDto> getUserModerationHistory(Long userId, int page, int limit) {

        Pageable pageable = PageRequest.of(page, limit);

        List<Event> eventList = eventRepository.findUserModerationHistory(userId, pageable);
        List<EventFullDto> fullEventDtos = emptyList();

        if (!eventList.isEmpty()) {
            List<Long> eventIds = eventList.stream()
                    .map(Event::getId)
                    .toList();

            // Получаем последние комментарии модерации для событий
            List<ModerationComment> moderationComments = moderationCommentRepository.findLastCommentsByEventIds(eventIds);
            // Создаём маппинг: eventId → ModerationComment
            Map<Long, ModerationComment> commentsMap = moderationComments.stream()
                    .collect(Collectors.toMap(
                            comment -> comment.getEvent().getId(),
                            Function.identity()
                    ));

            Map<Long, CategoryDto> idToCategory = getIdToCategoryForEvent(eventList);

            List<Long> userIds = eventList.stream().map(Event::getInitiatorId).distinct().toList();
            List<UserDto> userDtos = userClient.get(userIds, 0, userIds.size());
            Map<Long, UserDto> users = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));

            fullEventDtos = eventList.stream()
                    .map(event -> {
                        ModerationComment comment = commentsMap.get(event.getId());
                        return EventsMapper.toEventFullDto(
                                event,
                                event.getCategoryId() == null ? null : idToCategory.get(event.getCategoryId()),
                                users.get(event.getInitiatorId()),
                                comment);
                    })
                    .toList();
        }

        return fullEventDtos;
    }

    @Override
    public List<EventFullDto> getEventsForModeration(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        List<Event> events = eventRepository.findByRequestModerationAndState(
                Boolean.TRUE, EventState.PENDING, pageable
        );

        Map<Long, CategoryDto> idToCategory = getIdToCategoryForEvent(events);
        List<Long> userIds = events.stream().map(Event::getInitiatorId).distinct().toList();
        List<UserDto> userDtos = userClient.get(userIds, 0, userIds.size());
        Map<Long, UserDto> users = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));

        return events.stream()
                .map(event -> EventsMapper.toEventFullDto(
                        event,
                        event.getCategoryId() == null ? null : idToCategory.get(event.getCategoryId()),
                        users.get(event.getInitiatorId())
                ))
                .toList();
    }

    @Override
    public List<EventShortDto> findActualPublishedEventsBySubscriberId(Long id,
                                                                       List<Long> publisherIds,
                                                                       EventState state,
                                                                       LocalDateTime time,
                                                                       PageRequest pageRequest) {
        List<Event> events = eventRepository.findActualPublishedEventsBySubscriberId(publisherIds, state, time, pageRequest);
        Map<Long, Long> requestCounts = getRequestCounts(events.stream().map(Event::getId).toList());
        Map<Long, Long> ratingsMap = getRatingsMap(events);
        Map<Long, CategoryDto> idToCategory = getIdToCategoryForEvent(events);

        List<Long> userIds = events.stream().map(Event::getInitiatorId).distinct().toList();
        List<UserDto> userDtos = userClient.get(userIds, 0, userIds.size());
        Map<Long, UserDto> users = userDtos.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return events
                .stream()
                .map(event -> toShortEventDto(
                        event,
                        event.getCategoryId() == null ? null : idToCategory.get(event.getCategoryId()),
                        users.get(event.getInitiatorId()),
                        requestCounts.getOrDefault(event.getId(), 0L),
                        ratingsMap.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    @Override
    public EventFullDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        Map<Long, Long> eventToRequest =
                requestClient.countRequestsByEventIdsAndStatus(singletonList(event.getId()), EventState.CONFIRMED);

        event.setConfirmedRequests(eventToRequest.getOrDefault(event.getId(), 0L));
        setViewsToEvents(List.of(event));
        Long rating = rateClient.getRatingByEventId(id);
        UserDto user = userClient.getById(event.getInitiatorId());
        CategoryDto category = event.getCategoryId() == null ? null : categoryClient.getCategoryById(event.getCategoryId());
        return toEventFullDto(event, category, user, rating);
    }

    @Override
    public Boolean checkCategoryInUse(Long categoryId) {
        log.info("Получен запрос на проверку использования категории '{}' в событиях", categoryId);
        return eventRepository.existsByCategoryId(categoryId);
    }

    Map<Long, CategoryDto> getIdToCategoryForEvent(List<Event> events){
        if(events == null || events.isEmpty()){
            return  emptyMap();
        }

        List<Long> categoryIds = events.stream().map(Event::getCategoryId).toList();

        if(categoryIds.isEmpty()){
            return  emptyMap();
        }

        log.info("Отправляем запрос на получение категорий для ids: {}", categoryIds);
        List<CategoryDto> categoriesByIds = categoryClient.getCategoriesByIds(categoryIds);
        return categoriesByIds == null ?
                emptyMap() : categoriesByIds.stream().collect(Collectors.toMap(CategoryDto::getId, Function.identity()));
    }
}
