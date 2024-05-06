package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ItemRequestCreateDto {
    private String description;
    private Long requesterId;
    private LocalDateTime created = LocalDateTime.now(ZoneId.of("UTC+3"));
}
