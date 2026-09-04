package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.compilation.dal.model.Compilation;
import ru.practicum.compilation.dal.repository.CompilationRepository;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.events.EventShortDto;
import ru.practicum.exception.NotFoundException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventClient eventClient;

    /**
     * Создает новую подборку событий.
     *
     * @param dto DTO с данными для создания подборки
     * @return созданная подборка с заполненной статистикой просмотров и подтвержденных запросов
     */
    @Transactional
    @Override
    public CompilationDto createCompilation(NewCompilationDto dto) {
        log.info("Создание новой подборки: {}", dto.getTitle());

        List<EventShortDto> events = Collections.emptyList();
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            List<EventShortDto> eventByIds = eventClient.getEventByIds(dto.getEvents());
            events = eventByIds == null ? Collections.emptyList() : eventByIds;
        }

        Compilation compilation = CompilationMapper.toCompilation(dto, events);
        Compilation saved = compilationRepository.save(compilation);

        return mapToDtoWithStats(saved);
    }

    /**
     * Удаляет подборку по идентификатору.
     *
     * @param compId идентификатор подборки
     * @throws NotFoundException если подборка с указанным идентификатором не найдена
     */
    @Transactional
    @Override
    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки с ID: {}", compId);
        long deletedRows = compilationRepository.deleteCompilationById(compId);
        if (deletedRows == 0) throw new NotFoundException("Compilation with id=" + compId + " was not found");
    }

    /**
     * Обновляет данные подборки.
     *
     * @param compId  идентификатор подборки
     * @param request запрос с данными для обновления
     * @return обновленная подборка с заполненной статистикой
     * @throws NotFoundException если подборка с указанным идентификатором не найдена
     */
    @Transactional
    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        log.info("Обновление подборки с ID: {}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            compilation.setTitle(request.getTitle());
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getEvents() != null) {
            compilation.setEventIds(request.getEvents());
        }

        Compilation updated = compilationRepository.save(compilation);
        return mapToDtoWithStats(updated);
    }

    /**
     * Получает список подборок с пагинацией и опциональной фильтрацией по признаку закрепления.
     *
     * @param pinned фильтр по признаку закрепления (null - без фильтрации)
     * @param from   индекс первого элемента для пагинации
     * @param size   количество элементов на странице
     * @return список подборок с заполненной статистикой
     */
    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получение списка подборок (pinned={}, from={}, size={})", pinned, from, size);
        PageRequest pageRequest = PageRequest.of(from / size, size);

        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageRequest);
        } else {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        }

        return mapToDtoListWithStats(compilations);
    }

    /**
     * Получает подборку по идентификатору.
     *
     * @param compId идентификатор подборки
     * @return подборка с заполненной статистикой
     * @throws NotFoundException если подборка с указанным идентификатором не найдена
     */
    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Получение подборки с ID: {}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        return mapToDtoWithStats(compilation);
    }

    /**
     * Преобразует подборку в DTO с заполненной статистикой.
     *
     * @param compilation подборка для преобразования
     * @return DTO с данными подборки и статистикой
     */
    private CompilationDto mapToDtoWithStats(Compilation compilation) {
        return mapToDtoListWithStats(List.of(compilation)).getFirst();
    }

    /**
     * Преобразует список подборок в список DTO с заполненной статистикой.
     *
     * @param compilations список подборок для преобразования
     * @return список DTO с данными подборок и статистикой
     */
    private List<CompilationDto> mapToDtoListWithStats(List<Compilation> compilations) {
        List<Long> eventIds = compilations.stream()
                .map(Compilation::getEventIds)
                .flatMap(List::stream)
                .distinct()
                .toList();

        if (!eventIds.isEmpty()) {
            List<EventShortDto> eventShortDtos = eventClient.getEventByIds(eventIds);
            if (eventShortDtos == null || eventShortDtos.isEmpty()) {
                throw new IllegalStateException("Ошибка получения событий");
            }


            return compilations.stream()
                    .map(comp -> {

                        var events = eventShortDtos.stream()
                                .filter(e -> comp.getEventIds().contains(e.getId()))
                                .toList();
                        return CompilationMapper.toCompilationDto(comp, events);
                    })
                    .toList();
        }

        return compilations.stream()
                .map(comp -> CompilationMapper.toCompilationDto(comp, Collections.emptyList()))
                .toList();
    }
}
