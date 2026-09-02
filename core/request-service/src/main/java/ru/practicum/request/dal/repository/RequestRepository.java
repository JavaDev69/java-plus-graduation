package ru.practicum.request.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.events.EventState;
import ru.practicum.request.dal.model.EventToRequest;
import ru.practicum.request.dal.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    long countByEventIdAndStatus(Long eventId, EventState status);

    @Query("SELECT r.eventId AS eventId, COUNT(r) AS count " +
            "FROM ParticipationRequest r " +
            "WHERE r.eventId IN :eventIds AND r.status = :status " +
            "GROUP BY r.eventId")
    List<EventToRequest> countConfirmedRequestsByEventIds(
            @Param("eventIds") List<Long> eventIds,
            @Param("status") EventState status
    );

    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, EventState status);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long requesterId);

    @Override
    Optional<ParticipationRequest> findById(Long id);
}
