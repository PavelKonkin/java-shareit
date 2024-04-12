package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.UserMapper;

@Component
public class CommentMapper {
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    @Autowired
    public CommentMapper(UserMapper userMapper, ItemMapper itemMapper) {
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
    }

    public Comment convertDto(CommentDto commentDto) {
        return Comment.builder()
                .text(commentDto.getText())
                .created(commentDto.getCreated())
                .item(itemMapper.convertItemDto(commentDto.getItem()))
                .author(userMapper.convertUserDto(commentDto.getAuthor()))
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
