package ru.practicum.compilation.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.compilation.dal.model.Compilation;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.events.EventShortDto;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompilationMapper {

    public static Compilation toCompilation(NewCompilationDto dto, List<EventShortDto> events) {
        Compilation compilation = new Compilation();
        compilation.setTitle(dto.getTitle());
        compilation.setPinned(dto.getPinned() != null && dto.getPinned());

        List<Long> eventIds = events.stream().map(EventShortDto::getId).toList();
        compilation.setEventIds(eventIds);
        return compilation;
    }

    public static CompilationDto toCompilationDto(
            Compilation compilation,
            List<EventShortDto> shortEvents) {

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(shortEvents)
                .build();
    }

}
