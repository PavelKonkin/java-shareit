package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingCreateDto bookingDto);

    BookingDto confirmReject(int userId, int bookingId, String approved);

    BookingDto get(int userId, int bookingId);

    List<BookingDto> getAllForBooker(int userId, BookingStateDto bookingStateDto, Pageable page);

    List<BookingDto> getAllForOwner(int userId, BookingStateDto bookingStateDto, Pageable page);
}
