package ru.practicum.dto;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 19.08.2026 - 16:09
 * @project java-plus-graduation
 */
public interface StatsOperation {
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/hit")
    void saveHit(@RequestBody @Valid EndpointHit dto);

    @GetMapping("/stats")
    List<ViewStats> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique);
}
