package ru.practicum.rating.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.rating.dal.model.EventToRating;
import ru.practicum.rating.dal.model.Rate;

import java.util.List;
import java.util.Optional;

public interface RateRepository extends JpaRepository<Rate, Long> {

    Optional<Rate> findByEventIdAndUserId(Long eventId, Long userId);

    @Query("SELECT r.eventId AS eventId, " +
            "SUM(CASE WHEN r.isLike = true THEN 1 ELSE -1 END) AS rating " +
            "FROM Rate r " +
            "WHERE r.eventId IN :eventIds " +
            "GROUP BY r.eventId")
    List<EventToRating> getRatingsForEvents(@Param("eventIds") List<Long> eventIds);

    // Подсчет рейтинга для одного события (чтобы не гонять массивы ради 1 ивента)
    @Query("SELECT COALESCE(SUM(CASE WHEN r.isLike = true THEN 1 ELSE -1 END), 0) " +
            "FROM Rate r " +
            "WHERE r.eventId = :eventId")
    Long getRatingForEvent(@Param("eventId") Long eventId);
}