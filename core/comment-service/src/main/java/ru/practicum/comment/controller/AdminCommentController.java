package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comments.CommentDto;
import ru.practicum.dto.comments.UpdateCommentByModeratorRequest;
import ru.practicum.comment.service.CommentService;
import ru.practicum.operations.AdminCommentOperation;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Slf4j
public class AdminCommentController implements AdminCommentOperation{

    private final CommentService commentService;

    @Override
    public List<CommentDto> getAllComments(Long eventId,Integer from,Integer size) {
        log.info("Админ запрашивает комментарии");
        return commentService.getCommentsByEventId(eventId, from, size);
    }

    @Override
    public CommentDto moderateComment(Long commentId, UpdateCommentByModeratorRequest request) {
        log.info("Модерация комментария {}: статус={}", commentId, request.getStatus());
        return commentService.updateCommentByModerator(null, commentId, request);
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        log.info("Админ удаляет комментарий: {}", commentId);
        commentService.deleteCommentByModerator(null, commentId);
    }
}