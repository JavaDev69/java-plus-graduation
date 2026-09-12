package ru.practicum.analyzer.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.analyzer.dal.model.EventSimilarity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Andrew Vilkov
 * @created 10.09.2026 - 14:31
 * @project java-plus-graduation
 */
public interface SimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    Optional<EventSimilarity> findOneByFirstEventAndSecondEvent(Long firstEvent, Long secondEvent);

    @Query("""
            select e from EventSimilarity e
            where e.firstEvent in :ids or e.secondEvent in :ids
            """)
    List<EventSimilarity> findAllByFirstEventOrSecondEventIn(@Param("ids") Collection<Long> ids);

    @Query("""
            select e from EventSimilarity e
            where e.firstEvent in :ids or e.secondEvent in :ids
            order by e.similarity desc
            limit :limit
            """)
    List<EventSimilarity> findAllByEventIdOrderBySimilarityDesc(@Param("id") Long id, @Param("limit") int limit);
}
