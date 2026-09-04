package ru.practicum.operations;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.dto.comments.CommentDto;
import ru.practicum.dto.comments.UpdateCommentByModeratorRequest;

import java.util.List;

/**
 * @author Andrew Vilkov
 * @created 27.08.2026 - 15:47
 * @project java-plus-graduation
 */
public interface AdminCommentOperation {

    @GetMapping
    List<CommentDto> getAllComments(
            @RequestParam(required = false) Long eventId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size);

    @PatchMapping("/{commentId}")
    CommentDto moderateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentByModeratorRequest request);

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCommentByAdmin(@PathVariable Long commentId);
}
