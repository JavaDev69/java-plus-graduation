package ru.practicum.moderation;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import ru.practicum.StatsClient;
import ru.practicum.client.CategoryClient;
import ru.practicum.client.RateClient;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.EventState;
import ru.practicum.dto.events.Location;
import ru.practicum.events.dal.model.Event;
import ru.practicum.events.dal.repository.EventsRepository;
import ru.practicum.events.moderation.ModerationComment;
import ru.practicum.events.moderation.ModerationCommentRepository;
import ru.practicum.events.service.EventsService;
import ru.practicum.events.service.EventsServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = EventsServiceImpl.class,
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.config.location=classpath:application-test.properties"
        })
class EventServiceImplTest {
    @MockBean EventsRepository eventRepository;
    @MockBean RateClient rateClient;
    @MockBean CategoryClient categoryClient;
    @MockBean RequestClient requestClient;
    @MockBean UserClient userClient;
    @MockBean StatsClient statsClient;
    @MockBean ModerationCommentRepository moderationCommentRepository;
    @MockBean EntityManager entityManager;

    @Autowired
    private EventsService eventService;


    private static final Long USER_ID = 1L;
    private static final int FROM = 0;
    private static final int SIZE = 10;

    @BeforeEach
    void init() {
        when(categoryClient.getCategoriesByIds(any()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));
    }


    @Test
    void shouldReturnEmptyPageWhenNoEventsFound() {
        // Given
        Pageable pageable = PageRequest.of(FROM, SIZE);


        when(eventRepository.findUserModerationHistory(USER_ID, pageable)).thenReturn(Collections.emptyList());

        // When
        List<EventFullDto> result = eventService.getUserModerationHistory(USER_ID, FROM, SIZE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEmpty());
    }

    @Test
    void shouldHandleEventsWithoutModerationComments() {
        // Given
        Pageable pageable = PageRequest.of(FROM, SIZE);

        // Создаём тестовые события (1 событие)
        List<Event> events = createEventsWithoutComments();

        // Настраиваем моки: репозиторий возвращает события,
        // репозиторий комментариев возвращает пустой список
        when(eventRepository.findUserModerationHistory(USER_ID, pageable))
                .thenReturn(events);
        when(moderationCommentRepository.findLastCommentsByEventIds(anyList()))
                .thenReturn(Collections.emptyList());

        // When
        List<EventFullDto> result = eventService.getUserModerationHistory(USER_ID, FROM, SIZE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1); // Теперь ожидаем 1 событие

        EventFullDto eventDto = result.getFirst();
        assertThat(eventDto.getLastModerationCommentDto()).isNull();
    }


    @Test
    void shouldApplyCorrectPagination() {

        // Создаём 15 тестовых событий
        List<Event> allEvents = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            allEvents.add(createEvent((long) i + 1, USER_ID, EventState.CANCELED));
        }

        List<Event> pageEvents = allEvents.subList(5, 10);
        Pageable expectedPageable = PageRequest.of(1, 5);

        // Настраиваем мок: ожидаем вызов с PageRequest.of(1, 5)
        when(eventRepository.findUserModerationHistory(USER_ID, expectedPageable))
                .thenReturn(pageEvents);

        // Создаём комментарии для событий на текущей странице
        List<ModerationComment> comments = createTestModerationCommentsForEvents(pageEvents);
        when(moderationCommentRepository.findLastCommentsByEventIds(anyList()))
                .thenReturn(comments);

        // When
        // Передаём offset = 5, limit = 5 — сервис преобразует в page = 1
        List<EventFullDto> result = eventService.getUserModerationHistory(USER_ID, 1, 5);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(5); // Ожидаем 5 элементов на странице
    }


    @Test
    void shouldReturnEmptyListWhenNoEventsFound() {
        // Given
        Pageable pageable = PageRequest.of(FROM, SIZE);
        when(eventRepository.findUserModerationHistory(USER_ID, pageable))
                .thenReturn(Collections.emptyList());

        // When
        List<EventFullDto> result = eventService.getUserModerationHistory(USER_ID, FROM, SIZE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty(); // Явная проверка на пустой список
    }


    @Test
    void shouldFilterEventsByUserIdAndState() {
        // Given
        Pageable pageable = PageRequest.of(FROM, SIZE);

        // Создаём события с разными пользователями и состояниями
        Event event1 = createEvent(1L, USER_ID, EventState.PENDING); // Наш пользователь, нужное состояние
        Event event2 = createEvent(2L, USER_ID + 1, EventState.REJECTED); // Другой пользователь
        Event event3 = createEvent(3L, USER_ID, EventState.PUBLISHED); // Другое состояние

        List<Event> testEvents = Arrays.asList(event1, event2, event3);

        // Мок возвращает только события, соответствующие фильтру (event1)
        when(eventRepository.findUserModerationHistory(USER_ID, pageable))
                .thenReturn(Arrays.asList(event1));

        when(moderationCommentRepository.findLastCommentsByEventIds(anyList()))
                .thenReturn(Collections.emptyList());

        // When
        List<EventFullDto> result = eventService.getUserModerationHistory(USER_ID, FROM, SIZE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
    }


    // Вспомогательные методы для создания тестовых данных
    private List<Event> createTestEvents() {
        return Arrays.asList(
                createEvent(1L, USER_ID, EventState.CANCELED),
                createEvent(2L, USER_ID, EventState.REJECTED)
        );
    }

    private List<ModerationComment> createTestModerationComments() {
        ModerationComment comment1 = new ModerationComment();
        comment1.setId(101L);
        comment1.setCommentText("First moderation comment");
        comment1.setCreatedOn(LocalDateTime.now());

        Event event = new Event();
        event.setId(1L);
        comment1.setEvent(event);

        return Collections.singletonList(comment1);
    }

    private List<Event> createEventsWithoutComments() {
        return Collections.singletonList(createEvent(3L, USER_ID, EventState.CANCELED));
    }

    private Event createEvent(Long id, Long userId, EventState state) {
        Location location = new Location(55.751244F, 37.618423F);

        return Event.builder()
                .id(id)
                .annotation("Test annotation " + id)
                .categoryId(100L)
                .description("Test description " + id)
                .eventDate(LocalDateTime.now().plusDays(1))
                .locationLat(location.getLat())
                .locationLon(location.getLon())
                .paid(false)
                .participantLimit(0)
                .requestModeration(false)
                .title("Test event " + id)
                .state(state)
                .initiatorId(userId)
                .build();
    }

    private List<ModerationComment> createTestModerationCommentsForEvents(List<Event> events) {
        return events.stream()
                .map(event -> {
                    ModerationComment comment = new ModerationComment();
                    comment.setId(event.getId() + 1000); // Уникальный ID
                    comment.setCommentText("Moderation comment for event " + event.getId());
                    comment.setCreatedOn(LocalDateTime.now());

                    comment.setEvent(event);
                    return comment;
                })
                .toList();
    }
}

