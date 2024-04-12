package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Value;


@Value
@Builder(toBuilder = true)
public class BookingShortDto {
    int id;
    int bookerId;
}