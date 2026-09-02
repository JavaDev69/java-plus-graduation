package ru.practicum.comment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.comments.CommentDto;
import ru.practicum.dto.comments.CommentStatus;
import ru.practicum.dto.comments.NewCommentDto;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;

import java.time.LocalDateTime;
import java.time.ZoneId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Component
public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment, UserShortDto user) {
        if (comment == null) return null;
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .eventId(comment.getEventId())
                .authorId(user.getId())
                .authorName(user.getName())
                .status(comment.getStatus())
                .createdOn(comment.getCreatedOn())
                .updatedOn(comment.getUpdatedOn())
                .build();
    }

    public static Comment toComment(NewCommentDto dto, EventFullDto event, UserDto author) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setEventId(event.getId());
        comment.setAuthorId(author.getId());
        comment.setStatus(CommentStatus.PENDING);
        comment.setCreatedOn(LocalDateTime.now(ZoneId.systemDefault()));
        return comment;
    }
}
