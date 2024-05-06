package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;

public interface CommentService {
    CommentDto create(CommentCreateDto commentCreateDto);
}
