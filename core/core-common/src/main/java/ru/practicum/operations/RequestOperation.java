package ru.practicum.operations;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.events.EventState;

import java.util.List;
import java.util.Map;

/**
 * @author Andrew Vilkov
 * @created 25.08.2026 - 15:29
 * @project java-plus-graduation
 */
public interface RequestOperation {

    @GetMapping
    Map<Long, Long> countRequestsByEventIdsAndStatus(
            @RequestParam @NotEmpty List<Long> eventIds,
            @RequestParam EventState state);
}
