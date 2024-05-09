package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingCreateDto bookingDto);

    BookingDto confirmReject(long userId, long bookingId, String approved);

    BookingDto get(long userId, long bookingId);

    List<BookingDto> getAllForBooker(long userId, BookingStateDto bookingStateDto, Pageable page);

    List<BookingDto> getAllForOwner(long userId, BookingStateDto bookingStateDto, Pageable page);
}
