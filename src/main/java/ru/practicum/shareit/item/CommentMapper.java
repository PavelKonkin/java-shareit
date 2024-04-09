package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

@Component
public class CommentMapper {
    public Comment convertDto(CommentDto commentDto) {
        return Comment.builder()
                .text(commentDto.getText())
                .created(commentDto.getCreated())
                .item(commentDto.getItem())
                .author(commentDto.getAuthor())
                .build();
    }

    public CommentDto convertComment(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .authorName(comment.getAuthor().getName())
                .build();
    }
}
