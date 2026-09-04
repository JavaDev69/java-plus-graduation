package ru.practicum.request.service;

import ru.practicum.dto.events.EventState;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface RequestsService {
    EventRequestStatusUpdateResult updateRequestStatuses(
            Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    Map<Long, Long> countRequestsByEventIdsAndStatus(List<Long> eventIds, EventState state);
}
