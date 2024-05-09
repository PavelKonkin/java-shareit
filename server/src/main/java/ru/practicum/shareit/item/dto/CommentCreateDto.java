package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class CommentCreateDto {
    private String text;
    private Long authorId;
    private Long itemId;
    private LocalDateTime created = LocalDateTime.now();
}
