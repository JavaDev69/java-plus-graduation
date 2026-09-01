package ru.practicum.rating.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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