package ru.practicum.dto.request;

import lombok.Data;
import ru.practicum.dto.events.EventState;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private EventState status;
}

