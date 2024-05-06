package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class CommentDto {
    private Long id;
    private String text;
    private String authorName;
    private UserDto author;
    private ItemDto item;
    private LocalDateTime created;
}
