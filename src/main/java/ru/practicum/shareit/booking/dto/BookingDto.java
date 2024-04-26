package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.serializer.LocalDateTimeSerializer;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    private Integer id;
    @Future
    @NotNull
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime start;
    @Future
    @NotNull
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime end;
    private BookingStateDto status;
    private UserDto booker;
    private ItemDto item;
}
