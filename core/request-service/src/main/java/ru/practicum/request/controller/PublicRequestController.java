package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.events.EventState;
import ru.practicum.operations.RequestOperation;
import ru.practicum.request.service.RequestsService;

import java.util.List;
import java.util.Map;

/**
 * @author Andrew Vilkov
 * @created 28.08.2026 - 12:26
 * @project java-plus-graduation
 */
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicRequestController implements RequestOperation {
    private final RequestsService requestsService;

    @Override
    public Map<Long,Long> countRequestsByEventIdsAndStatus(List<Long> eventIds, EventState state) {
        return requestsService.countRequestsByEventIdsAndStatus(eventIds,state);
    }
}
