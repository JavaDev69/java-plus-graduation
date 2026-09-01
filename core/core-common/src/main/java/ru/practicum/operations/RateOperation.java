package ru.practicum.operations;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author Andrew Vilkov
 * @created 28.08.2026 - 14:42
 * @project java-plus-graduation
 */
public interface RateOperation {

    @GetMapping("/{eventId}")
    Long getRatingByEventId(@PathVariable(name = "eventId") Long id);

    @GetMapping
    Map<Long,Long> getRatingByEventIds(@RequestBody @NotEmpty List<Long> eventIds);
}
