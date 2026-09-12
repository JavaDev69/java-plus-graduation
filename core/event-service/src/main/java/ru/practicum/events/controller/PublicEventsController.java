package ru.practicum.events.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.EventShortDto;
import ru.practicum.dto.events.EventState;
import ru.practicum.events.dal.model.EventsSortType;
import ru.practicum.events.service.EventsService;
import ru.practicum.operations.EventOperation;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventsController implements EventOperation {
    private final EventsService eventService;

    @Override
    public List<EventShortDto> getEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            String sort,
            Integer from,
            Integer size
    ) {
        return eventService.getPublishedEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                EventsSortType.valueOf(sort), from, size
        );
    }

    @Override
    public EventFullDto getPublishedEventById(long userId, Long id) {
        return eventService.getPublishedEventById(userId, id);
    }

    @Override
    public EventFullDto getEventById(Long id) {

        return eventService.getEventById(id);
    }

    @Override
    public List<EventShortDto> getEventByIds(List<Long> ids) {
        return eventService.getShortEventByIds(ids);
    }

    @Override
    public List<EventShortDto> getActualPublishedEventsBySubscriberId(Long id,
                                                                      List<Long> publisherIds,
                                                                      EventState state,
                                                                      LocalDateTime time,
                                                                      Integer from,
                                                                      Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return eventService.findActualPublishedEventsBySubscriberId(id, publisherIds, state, time, pageRequest);
    }

    @Override
    public Boolean checkCategoryInUse(Long categoryId) {
        return eventService.checkCategoryInUse(categoryId);
    }

    @Override
    public List<EventShortDto> getRecommendations(long userId) {
        return eventService.getRecommendations(userId);
    }

    @Override
    public void addLikeToEvent(long userId, Long eventId) {
        eventService.addLikeToEvent(userId,eventId);
    }
}
