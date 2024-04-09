package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class CommentCreateDto {
    @NotBlank
    private String text;
    private Integer authorId;
    private Integer itemId;
    private LocalDateTime created = LocalDateTime.now();
}
