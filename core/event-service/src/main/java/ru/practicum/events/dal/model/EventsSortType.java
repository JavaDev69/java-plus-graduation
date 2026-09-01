package ru.practicum.events.dal.model;

import lombok.Getter;

@Getter
public enum EventsSortType {
    VIEWS("VIEWS"),
    RATING("RATING"),
    EVENT_DATE("EVENT_DATE");

    private final String value;

    EventsSortType(String value) {
        this.value = value;
    }
}
