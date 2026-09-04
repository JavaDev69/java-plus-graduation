package ru.practicum.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.UpdateEventAdminRequest;
import ru.practicum.events.service.EventsService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventsController {

    private final EventsService adminEventService;

    @GetMapping
    public List<EventFullDto> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("Получен запрос на получение списка обо всех событиях подходящих под переданные условия");
        List<EventFullDto> events = adminEventService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
        log.info("Получен список событий подходящих под условия. Количество элементов: {}", events.size());
        return events;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByAdmin(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest updateRequest
    ) {
        log.info("Получен запрос на редактирование события с ID: {}", eventId);
        EventFullDto updatedEvent = adminEventService.updateEventByAdmin(eventId, updateRequest);
        log.info("Собьытие успешно отредактировано. {}", updatedEvent);
        return updatedEvent;
    }

    @GetMapping("/moderation")
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEventsForModeration(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("Получен запрос на получение списка событий для модерации. Параметры: from={}, size={}", from, size);

        List<EventFullDto> events = adminEventService.getEventsForModeration(from, size);

        log.info("Получен список событий для модерации. Количество элементов: {}", events.size());

        return events;
    }
}
