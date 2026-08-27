package ru.practicum.comments;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.comments.CommentDto;
import ru.practicum.dto.comments.CommentStatus;
import ru.practicum.dto.comments.NewCommentDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.events.Event;

import java.time.LocalDateTime;
import java.time.ZoneId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Component
public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) return null;
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .eventId(comment.getEvent().getId())
                .authorId(comment.getAuthorId())
                //todo fix it
                .authorName(comment.getAuthorId().toString())
                .status(comment.getStatus())
                .createdOn(comment.getCreatedOn())
                .updatedOn(comment.getUpdatedOn())
                .build();
    }

    public static Comment toComment(NewCommentDto dto, Event event, UserDto author) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setEvent(event);
        comment.setAuthorId(author.getId());
        comment.setStatus(CommentStatus.PENDING);
        comment.setCreatedOn(LocalDateTime.now(ZoneId.systemDefault()));
        return comment;
    }
}
