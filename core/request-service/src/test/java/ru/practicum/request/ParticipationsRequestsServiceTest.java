package ru.practicum.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.events.EventState;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dal.model.ParticipationRequest;
import ru.practicum.request.dal.repository.RequestRepository;
import ru.practicum.request.participation.ParticipationsRequestsService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipationsRequestsServiceTest {

    @Mock
    UserClient userClient;

    @Mock
    EventClient eventClient;

    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private ParticipationsRequestsService participationsRequestsService;

    @Test
    void createParticipationRequest_UserNotFound_ThrowsNotFoundException() {
        // Given
        Long userId = 999L;
        Long eventId = 100L;

        when(userClient.getById(userId))
                .thenThrow(new NotFoundException("Пользователь с ID 999 не найден"));

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> participationsRequestsService.createParticipationRequest(userId, eventId));

        assertTrue(exception.getMessage().contains("Пользователь с ID 999 не найден"));
    }

    @Test
    void cancelParticipationRequest_RequestNotFound_ThrowsNotFoundException() {
        // Given
        Long userId = 1L;
        Long requestId = 999L;

        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> participationsRequestsService.cancelParticipationRequest(userId, requestId));

        assertTrue(exception.getMessage().contains("Request with id=" + requestId + " was not found"));
    }

    @Test
    void cancelParticipationRequest_WrongUser_ThrowsNotFoundException() {
        // Given
        Long userId = 1L;
        Long otherUserId = 2L;
        Long requestId = 1000L;

        ParticipationRequest request = new ParticipationRequest();
        request.setId(requestId);
        request.setRequesterId(otherUserId);
        request.setStatus(EventState.PENDING);

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> participationsRequestsService.cancelParticipationRequest(userId, requestId));

        assertTrue(exception.getMessage().contains("is not accessible for user " + userId));
    }

    @Test
    void cancelParticipationRequest_AlreadyConfirmed_ThrowsIllegalArgumentException() {
        // Given
        Long userId = 1L;
        Long requestId = 1000L;

        ParticipationRequest request = new ParticipationRequest();
        request.setId(requestId);
        request.setRequesterId(userId);
        request.setStatus(EventState.CONFIRMED);

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> participationsRequestsService.cancelParticipationRequest(userId, requestId));

        assertTrue(exception.getMessage().contains("Cannot cancel request with status: " + EventState.CONFIRMED));
    }

    @Test
    void cancelParticipationRequest_AlreadyCancelled_ThrowsIllegalArgumentException() {
        // Given
        Long userId = 1L;
        Long requestId = 1000L;

        ParticipationRequest request = new ParticipationRequest();
        request.setId(requestId);
        request.setRequesterId(userId);
        request.setStatus(EventState.CANCELED);

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> participationsRequestsService.cancelParticipationRequest(userId, requestId));

        assertTrue(exception.getMessage().contains("Cannot cancel request with status: " + EventState.CANCELED));
    }

    @Test
    void getUserParticipationRequests_Success_EmptyList() {
        // Given
        Long userId = 1L;

        UserDto user = UserDto.builder().id(userId).build();

        when(userClient.getById(userId)).thenReturn(user);
        when(requestRepository.findByRequesterId(userId)).thenReturn(Collections.emptyList());

        // When
        List<ParticipationRequestDto> result = participationsRequestsService.getUserParticipationRequests(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userClient, times(1)).getById(userId);
        verify(requestRepository, times(1)).findByRequesterId(userId);
    }

    @Test
    void getUserParticipationRequests_Success_WithRequests() {
        // Given
        Long userId = 1L;

        UserDto user = UserDto.builder().id(userId).build();

        ParticipationRequest request1 = new ParticipationRequest();
        request1.setId(1000L);
        request1.setRequesterId(userId);
        request1.setEventId(100L);
        request1.setStatus(EventState.PENDING);
        request1.setCreated(LocalDateTime.now().minusDays(1));

        ParticipationRequest request2 = new ParticipationRequest();
        request2.setId(1001L);
        request2.setRequesterId(userId);
        request2.setEventId(200L);
        request2.setStatus(EventState.CONFIRMED);
        request2.setCreated(LocalDateTime.now());

        List<ParticipationRequest> requests = List.of(request1, request2);

        when(userClient.getById(userId)).thenReturn(user);
        when(requestRepository.findByRequesterId(userId)).thenReturn(requests);

        // When
        List<ParticipationRequestDto> result = participationsRequestsService.getUserParticipationRequests(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        ParticipationRequestDto dto1 = result.getFirst();
        assertEquals(1000L, dto1.getId());
        assertEquals(EventState.PENDING, dto1.getStatus());
        assertEquals(100L, dto1.getEvent());

        ParticipationRequestDto dto2 = result.get(1);
        assertEquals(1001L, dto2.getId());
        assertEquals(EventState.CONFIRMED, dto2.getStatus());
        assertEquals(200L, dto2.getEvent());
    }

    @Test
    void getUserParticipationRequests_UserNotFound_ThrowsNotFoundException() {
        // Given
        Long userId = 999L;

        when(userClient.getById(userId))
                .thenThrow(new NotFoundException("Пользователь с ID 999 не найден"));

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> participationsRequestsService.getUserParticipationRequests(userId));

        assertTrue(exception.getMessage().contains("Пользователь с ID 999 не найден"));
    }

}
