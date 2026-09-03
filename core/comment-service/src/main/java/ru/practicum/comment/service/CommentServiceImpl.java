package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.comment.Comment;
import ru.practicum.comment.CommentMapper;
import ru.practicum.comment.CommentRepository;
import ru.practicum.dto.comments.CommentDto;
import ru.practicum.dto.comments.CommentStatus;
import ru.practicum.dto.comments.NewCommentDto;
import ru.practicum.dto.comments.UpdateCommentByAuthorRequest;
import ru.practicum.dto.comments.UpdateCommentByModeratorRequest;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Transactional
    @Override
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto dto) {
        UserDto author = userClient.getById(userId);
        ResponseEntity<EventFullDto> eventById = eventClient.getPublishedEventById(eventId);
        if (eventById.getBody() == null) {
            throw new NotFoundException("Событие с ID " + eventId + " не найдено");
        }

        EventFullDto event = eventById.getBody();

        Comment comment = CommentMapper.toComment(dto, event, author);
        Comment saved = commentRepository.save(comment);

        return CommentMapper.toCommentDto(saved, author);
    }

    @Override
    @Transactional
    public CommentDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        UserDto author = userClient.getById(comment.getAuthorId());
        return CommentMapper.toCommentDto(comment, author);
    }

    @Override
    @Transactional
    public List<CommentDto> getCommentsByEventId(Long eventId, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Comment> byEventIdAndStatus = commentRepository.findByEventIdAndStatus(eventId, CommentStatus.APPROVED, page);
        if (byEventIdAndStatus.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> authorIds = byEventIdAndStatus.stream().map(Comment::getAuthorId).distinct().toList();
        log.info("Список id авторов для получения UserDto: {}", authorIds);
        List<UserDto> userDtos = userClient.get(authorIds, 0, authorIds.size());
        Map<Long, UserDto> users = userDtos.stream().collect(Collectors.toMap(UserShortDto::getId, Function.identity()));

        return byEventIdAndStatus.stream()
                .map(comment -> CommentMapper.toCommentDto(
                        comment,
                        users.get(comment.getAuthorId())
                ))
                .toList();
    }

    @Override
    @Transactional
    public CommentDto updateCommentByAuthor(Long userId, Long commentId, UpdateCommentByAuthorRequest request) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();

        if (!comment.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("Only author can edit comment");
        }
        if (comment.getStatus() != CommentStatus.PENDING && comment.getStatus() != CommentStatus.APPROVED) {
            throw new IllegalArgumentException("Cannot edit this comment");
        }

        if (request.getText() != null) {
            comment.setText(request.getText());
        }
        comment.setUpdatedOn(LocalDateTime.now());

        UserDto user = userClient.getById(comment.getAuthorId());
        return CommentMapper.toCommentDto(commentRepository.save(comment), user);
    }

    @Override
    @Transactional
    public CommentDto updateCommentByModerator(Long userId, Long commentId, UpdateCommentByModeratorRequest request) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();

        comment.setStatus(request.getStatus());
        if (request.getText() != null) {
            comment.setText(request.getText());
        }
        comment.setUpdatedOn(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);
        log.info("Comment {} updated by moderator {}: status={}, text={}",
                saved.getId(), userId, saved.getStatus(), saved.getText());

        UserDto user = userClient.getById(comment.getAuthorId());
        return CommentMapper.toCommentDto(saved, user);
    }

    @Transactional
    @Override
    public void deleteCommentByAuthor(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own comments");
        }
        if (comment.getStatus() == CommentStatus.REJECTED) {
            throw new IllegalArgumentException("Cannot delete rejected comment");
        }

        commentRepository.deleteById(commentId);
    }

    @Transactional
    @Override
    public void deleteCommentByModerator(Long userId, Long commentId) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        commentRepository.deleteById(commentId);
    }
}
