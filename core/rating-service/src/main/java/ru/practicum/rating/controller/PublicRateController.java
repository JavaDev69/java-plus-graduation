package ru.practicum.rating.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.operations.RateOperation;
import ru.practicum.rating.service.RateService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rate")
@RequiredArgsConstructor
public class PublicRateController implements RateOperation {

    private final RateService rateService;

    @Override
    public Long getRatingByEventId(Long id) {
        return rateService.getRatingByEventId(id);
    }

    @Override
    public Map<Long, Long> getRatingByEventIds(List<Long> eventIds) {
        return rateService.getRatingByEventIds(eventIds);
    }
}