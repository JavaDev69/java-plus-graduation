package ru.practicum.analyzer.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.analyzer.dal.model.Interaction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 14:30
 * @project java-plus-graduation
 */
public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    Optional<Interaction> findOneByUserIdAndEventId(Long userId, Long eventId);

    List<Interaction> findAllByUserIdOrderByTimestampDesc(Long userId);

    Stream<Interaction> streamAllByEventIdIn(Collection<Long> eventIds);
}
