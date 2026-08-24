package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.StatsOperation;
import ru.practicum.dto.ViewStats;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController implements StatsOperation {
    private final StatsService statsService;

    @Override
    public ResponseEntity<Void> saveHit(EndpointHit dto) {
        statsService.saveHit(dto);
        return ResponseEntity.status(201).build();
    }

    @Override
    public ResponseEntity<List<ViewStats>> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            Boolean unique) {
        List<ViewStats> stats = statsService.getStats(start, end, uris, unique);
        return ResponseEntity.ok(stats);
    }
}
