package ru.practicum.request.participation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.EventState;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dal.model.ParticipationRequest;
import ru.practicum.request.dal.repository.RequestRepository;
import ru.practicum.request.mapper.RequestsMapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.request.mapper.RequestsMapper.toDto;

@AllArgsConstructor
@Service
@Slf4j
public class ParticipationsRequestsService {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserClient userClient;
    private final EventClient eventClient;
    private RequestRepository requestRepository;

    @Transactional
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {
        // 1. Проверяем существование пользователя
        UserDto requester = userClient.getById(userId);

        // 2. Проверяем существование события
        ResponseEntity<EventFullDto> eventById = eventClient.getEventById(eventId);
        if (eventById.getBody() == null) {
            throw new NotFoundException("Событие с ID " + eventId + " не найдено");
        }

        EventFullDto event = eventById.getBody();
        log.info("Получили событие: {}", event);

        // 3. Проверяем, что пользователь не является инициатором события
        if (Objects.equals(event.getInitiator().getId(), requester.getId())) {
            throw new ConflictException("User cannot request participation in their own event");
        }

        // 4. Проверяем статус события — должно быть PUBLISHED
        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        // 5. Проверяем отсутствие дубликата заявки
        boolean hasExistingRequest = requestRepository.existsByEventIdAndRequesterId(eventId, userId);
        if (hasExistingRequest) {
            throw new ConflictException("Duplicate participation request");
        }

        // 6. Проверяем лимит заявок
        if (event.getParticipantLimit() > 0) {
            long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, EventState.CONFIRMED);
            if (confirmedRequests >= event.getParticipantLimit()) {
                throw new ConflictException("Event participant limit reached");
            }
        }

        // 7. Создаём заявку
        ParticipationRequest request = new ParticipationRequest();
        request.setEventId(event.getId());
        request.setRequesterId(requester.getId());
        request.setCreated(LocalDateTime.now(ZoneId.systemDefault()));
        log.info("Заявка при создании в методе {}", request);

        // 8. Устанавливаем статус с учётом лимита участников и настройки модерации
        if (event.getParticipantLimit() == 0) {
            request.setStatus(EventState.CONFIRMED);
            log.info("Автоподтверждение: лимит участников 0, статус установлен как CONFIRMED");
        } else if (Boolean.FALSE.equals(event.getRequestModeration())) {
            request.setStatus(EventState.CONFIRMED);
            log.info("Модерация отключена, статус установлен как CONFIRMED");
        } else {
            request.setStatus(EventState.PENDING);
            log.info("Требуется модерация, статус установлен как PENDING");
        }

        ParticipationRequest savedRequest = requestRepository.save(request);
        savedRequest.setRequesterId(requester.getId());
        savedRequest.setEventId(event.getId());
        log.debug("Дата создания в БД (после сохранения): {}\nСтроковое представление даты в DTO: {}",
                request.getCreated(), savedRequest.getCreated().format(FORMATTER));

        log.info("Создана заявка на участие с ID: {}, статус: {}", savedRequest.getId(), savedRequest.getStatus());

        return toDto(savedRequest);
    }

    @Transactional
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        // 1. Проверяем существование запроса
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        // 2. Проверяем, что запрос принадлежит пользователю
        if (!request.getRequesterId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId + " is not accessible for user " + userId);
        }

        // 3. Проверяем статус запроса — можно отменять только PENDING
        if (!EventState.PENDING.equals(request.getStatus())) {
            throw new IllegalArgumentException("Cannot cancel request with status: " + request.getStatus());
        }

        // 4. Обновляем статус на CANCELLED
        request.setStatus(EventState.CANCELED);

        ParticipationRequest savedRequest = requestRepository.save(request);

        log.debug("Дата создания в БД (до отмены): {}\nСтроковое представление даты в DTO после отмены: {}",
                request.getCreated(), toDto(savedRequest).getCreated());

        log.info("Заявка на участие с ID: {} отменена пользователем: {}", requestId, userId);
        return toDto(savedRequest);
    }

    public List<ParticipationRequestDto> getUserParticipationRequests(Long userId) {
        // 1. Проверяем существование пользователя
        UserDto user = userClient.getById(userId);

        // 2. Получаем все заявки пользователя
        List<ParticipationRequest> requests = requestRepository.findByRequesterId(user.getId());

        // 3. Преобразуем в DTO
        return requests.stream()
                .map(RequestsMapper::toDto)
                .collect(Collectors.toList());
    }
}
