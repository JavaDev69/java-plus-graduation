package ru.practicum.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.common.Constance;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.request.dal.model.ParticipationRequest;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RequestsMapper {

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .created(request.getCreated().format(Constance.FORMATTER))
                .event(request.getEventId())
                .id(request.getId())
                .requester(request.getRequesterId())
                .status(request.getStatus())
                .build();
    }

    public static List<ParticipationRequestDto> toDtoList(List<ParticipationRequest> requests) {
        return requests.stream()
                .map(RequestsMapper::toDto)
                .collect(Collectors.toList());
    }
}
