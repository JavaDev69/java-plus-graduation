package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventClient eventClient;
//    private final RequestClient requestClient;
//    private final RateClient rateClient;
//    private final StatsClient statsClient;

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

        List<EventShortDto> events = new ArrayList<>();
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            ResponseEntity<List<EventShortDto>> eventByIds = eventClient.getEventByIds(dto.getEvents());
            events = eventByIds.getBody() == null ? Collections.emptyList() : eventByIds.getBody();
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
            ResponseEntity<List<EventShortDto>> eventByIds = eventClient.getEventByIds(eventIds);
            if (eventByIds.getBody() == null || eventByIds.getBody().isEmpty()) {
                throw new IllegalStateException("Ошибка получения событий");
            }

            return compilations.stream()
                    .map(comp -> CompilationMapper.toCompilationDto(comp, eventByIds.getBody()))
                    .toList();
        }

        return compilations.stream()
                .map(comp -> CompilationMapper.toCompilationDto(comp, Collections.emptyList()))
                .toList();
    }

//    private Map<Long, Long> getRatingsMap(List<Event> events) {
//        if (events.isEmpty()) return Collections.emptyMap();
//        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
//        List<Object[]> results = rateRepository.getRatingsForEvents(eventIds);
//        return results.stream().collect(Collectors.toMap(
//                row -> ((Number) row[0]).longValue(),
//                row -> ((Number) row[1]).longValue()
//        ));
//    }

//    /**
//     * Получает карту количества подтвержденных запросов для списка событий.
//     *
//     * @param events список событий
//     * @return карта, где ключ - идентификатор события, значение - количество подтвержденных запросов
//     */
//    private Map<Long, Long> getConfirmedRequestsMap(List<Event> events) {
//        if (events.isEmpty()) return Map.of();
//
//        List<Long> eventIds = events.stream()
//                .map(Event::getId)
//                .toList();
//
//        List<Object[]> results = requestRepository.countConfirmedRequestsByEventIds(eventIds, EventState.CONFIRMED);
//
//        return results.stream()
//                .collect(Collectors.toMap(
//                        row -> ((Number) row[0]).longValue(),
//                        row -> ((Number) row[1]).longValue()
//                ));
//    }

//    /**
//     * Получает карту количества просмотров для списка событий из сервиса статистики.
//     *
//     * @param events список событий
//     * @return карта, где ключ - идентификатор события, значение - количество просмотров
//     */
//    private Map<Long, Long> getViewsMap(List<Event> events) {
//        if (events.isEmpty()) return Collections.emptyMap();
//
//        List<String> uris = events.stream()
//                .map(e -> "/events/" + e.getId())
//                .toList();
//
//        LocalDateTime start = LocalDateTime.now().minusYears(10);
//        LocalDateTime end = LocalDateTime.now();
//
//        List<ViewStats> stats;
//        try {
//            ResponseEntity<List<ViewStats>> response = statsClient.getStats(start, end, uris, true);
//            stats = response.getBody();
//        } catch (Exception e) {
//            log.error("Ошибка при получении статистики", e);
//            return Collections.emptyMap();
//        }
//
//        Map<Long, Long> viewsMap = new HashMap<>();
//        for (ViewStats stat : stats) {
//            String uri = stat.getUri();
//            if (uri.startsWith("/events/")) {
//                try {
//                    Long eventId = Long.parseLong(uri.substring("/events/".length()));
//                    viewsMap.put(eventId, stat.getHits());
//                } catch (NumberFormatException ignored) {
//                }
//            }
//        }
//        return viewsMap;
//    }
}
