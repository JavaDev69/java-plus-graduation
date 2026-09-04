package ru.practicum.events.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.practicum.StatsClient;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.EventShortDto;
import ru.practicum.dto.events.EventState;
import ru.practicum.events.dal.model.EventsSortType;
import ru.practicum.events.service.EventsService;
import ru.practicum.operations.EventOperation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventsController implements EventOperation {
    @Value("${spring.application.name}")
    private String serviceName;
    private final EventsService eventService;
    private final StatsClient statsClient;

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
        saveHitInfo();
        return eventService.getPublishedEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                EventsSortType.valueOf(sort), from, size
        );
    }

    @Override
    public EventFullDto getPublishedEventById(Long id) {
        saveHitInfo();

        return eventService.getPublishedEventById(id);
    }

    @Override
    public EventFullDto getEventById(Long id) {
        saveHitInfo();

        return eventService.getEventById(id);
    }

    @Override
    public List<EventShortDto> getEventByIds(List<Long> ids) {
        return eventService.getShortEventByIds(ids);
    }

    private void saveHitInfo() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        EndpointHit hit = EndpointHit.builder()
                .app(serviceName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now(ZoneId.systemDefault()))
                .build();
        statsClient.saveHit(hit);
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

}
