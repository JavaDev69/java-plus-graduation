package ru.practicum.dto.events;

import lombok.Data;
import lombok.ToString;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.dto.user.UserShortDto;

@ToString
@Data
public class EventShortDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    private String eventDate;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private Long views;
    private Long rating;
}
