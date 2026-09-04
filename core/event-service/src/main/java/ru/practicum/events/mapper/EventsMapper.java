package ru.practicum.events.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.common.Constance;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.EventShortDto;
import ru.practicum.dto.events.EventState;
import ru.practicum.dto.events.Location;
import ru.practicum.dto.events.NewEventDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.events.dal.model.Event;
import ru.practicum.events.moderation.ModerationComment;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static ru.practicum.events.moderation.ModerationMapper.moderationCommentShortDto;

@UtilityClass
public class EventsMapper {

    public static EventShortDto toShortEventDto(Event event, CategoryDto category, UserShortDto userShortDto, Long confirmedRequests) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(category);
        dto.setConfirmedRequests(confirmedRequests);
        dto.setEventDate(event.getEventDate().format(Constance.FORMATTER));
        dto.setInitiator(userShortDto);
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setViews(event.getViews());
        return dto;
    }

    public static EventShortDto toShortEventDto(Event event, CategoryDto category, UserShortDto userShortDto, Long confirmedRequests, Long rating) {
        EventShortDto dto = toShortEventDto(event, category, userShortDto, confirmedRequests);
        dto.setRating(rating != null ? rating : 0L);
        return dto;
    }

    public static EventFullDto toEventFullDto(Event event, CategoryDto category, UserShortDto userShortDto, Long rating) {
        EventFullDto dto = toEventFullDto(event, category, userShortDto);
        dto.setRating(rating != null ? rating : 0L);
        return dto;
    }

    public static EventFullDto toEventFullDto(Event event, CategoryDto category, UserShortDto userShortDto) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(category);
        dto.setConfirmedRequests(event.getConfirmedRequests());
        dto.setCreatedOn(format(event.getCreatedOn()));
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate().format(Constance.FORMATTER));
        dto.setInitiator(userShortDto);
        dto.setLocation(new Location(event.getLocationLat(), event.getLocationLon()));
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setPublishedOn(event.getPublishedOn() != null ? format(event.getPublishedOn()) : null);
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState());
        dto.setTitle(event.getTitle());
        dto.setViews(event.getViews());
        return dto;
    }

    public static EventFullDto toEventFullDto(
            Event event,
            CategoryDto category,
            UserShortDto userShortDto,
            ModerationComment mc,
            Long rating
    ) {
        EventFullDto dto = toEventFullDto(event, category, userShortDto, rating);
        if (mc != null) {
            dto.setLastModerationCommentDto(moderationCommentShortDto(mc));
        }
        return dto;
    }

    public static EventFullDto toEventFullDto(
            Event event,
            CategoryDto category,
            UserShortDto userShortDto,
            ModerationComment mc
    ) {
        EventFullDto dto = toEventFullDto(event, category, userShortDto);
        if (mc != null) {
            dto.setLastModerationCommentDto(moderationCommentShortDto(mc));
        }
        return dto;
    }

    /**
     * Преобразует DTO нового события в сущность Event.
     *
     * @param dto  DTO с данными нового события
     * @param user пользователь-инициатор события
     * @return сущность Event, готовая для сохранения в БД
     */
    public static Event toEvent(NewEventDto dto, UserDto user, Long categoryId) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .categoryId(categoryId)
                .description(dto.getDescription())
                .title(dto.getTitle())
                .eventDate(dto.getEventDate())
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .locationLat(dto.getLocation().getLat())
                .locationLon(dto.getLocation().getLon())
                .createdOn(LocalDateTime.now(ZoneId.systemDefault()))
                .state(EventState.PENDING)
                .initiatorId(user.getId())
                .confirmedRequests(0L)
                .views(0L)
                .build();
    }

    private static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(Constance.FORMATTER) : null;
    }
}
