package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingCreateDto bookingDto);

    BookingDto confirmReject(int userId, int bookingId, String approved);

    BookingDto get(int userId, int bookingId);

    List<BookingDto> getAllForBooker(int userId, String state);

    List<BookingDto> getAllForOwner(int userId, String state);
}
