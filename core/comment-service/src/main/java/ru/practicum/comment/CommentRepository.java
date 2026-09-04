package ru.practicum.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.comments.CommentStatus;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status, Pageable pageable);

    List<Comment> findByEventId(Long eventId, Pageable pageable);

    @Query("SELECT COUNT(c) > 0 FROM Comment c " +
            "WHERE c.eventId = :eventId " +
            "AND c.authorId = :userId " +
            "AND c.status IN :statuses")
    boolean existsByEventIdAndAuthorIdAndStatusIn(
            @Param("eventId") Long eventId,
            @Param("userId") Long userId,
            @Param("statuses") List<CommentStatus> statuses);
}
