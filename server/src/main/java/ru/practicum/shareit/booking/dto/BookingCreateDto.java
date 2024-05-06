package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BookingCreateDto {
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStateDto status = BookingStateDto.WAITING;
    private long bookerId;
    private long itemId;
}
