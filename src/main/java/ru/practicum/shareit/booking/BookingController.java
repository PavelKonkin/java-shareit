package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.constant.Constants;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto create(@Valid @RequestBody BookingCreateDto bookingCreateDto,
                          @NotEmpty @RequestHeader(Constants.USER_HEADER) int userId) {
        bookingCreateDto.setBookerId(userId);
        log.info("Получен запрос на создание бронирования {} с id пользоателя {}", bookingCreateDto, userId);
        BookingDto bookingDto = bookingService.create(bookingCreateDto);
        log.info("Создано бронирование {}", bookingDto);
        return bookingDto;
    }

    @PatchMapping("/{bookingId}")
    BookingDto confirmReject(@NotEmpty @RequestHeader(Constants.USER_HEADER) int userId,
                             @PathVariable int bookingId, @RequestParam String approved) {
        log.info("Получен запрос на изменение статуса бронирования с id {} пользоателем с id {} на {}",
                bookingId, userId, approved);
        BookingDto bookingDto = bookingService.confirmReject(userId, bookingId, approved);
        log.info("Изменен статус бронирования {} вещи {} на {}",
                bookingId, bookingDto.getItem(), bookingDto.getStatus());
        return bookingDto;

    }

    @GetMapping("/{bookingId}")
    BookingDto get(@NotEmpty @RequestHeader(Constants.USER_HEADER) int userId, @PathVariable int bookingId) {
        log.info("Получен запрос на получение бронирования с id {} пользоателем с id {}",
                bookingId, userId);
        BookingDto bookingDto = bookingService.get(userId, bookingId);
        log.info("Найдено бронирования {} вещи {}",
                bookingId, bookingDto.getItem());
        return bookingDto;
    }

    @GetMapping
    List<BookingDto> getAll(@NotEmpty @RequestHeader(Constants.USER_HEADER) int userId,
                            @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получен запрос на получение списка бронирований пользоателя с id {} со статусом {}",
                userId, state);
        BookingStateDto bookingStateDto = getBookingStateValue(state);
        List<BookingDto> bookingDto = bookingService.getAllForBooker(userId, bookingStateDto);
        log.info("Сформирован список бронирований {} пользователя с id {}",
                bookingDto, userId);
        return bookingDto;
    }

    @GetMapping("/owner")
    List<BookingDto> getAllByOwner(@NotEmpty @RequestHeader(Constants.USER_HEADER) int userId,
                                   @RequestParam(defaultValue = "ALL") String state) {
        log.info("Получен запрос на получение списка бронирований вещей пользоателя с id {} со статусом {}",
                userId, state);
        BookingStateDto bookingStateDto = getBookingStateValue(state);
        List<BookingDto> bookingDto = bookingService.getAllForOwner(userId, bookingStateDto);
        log.info("Сформирован список бронирований вещей {} пользователя с id {}",
                bookingDto, userId);
        return bookingDto;
    }

    private BookingStateDto getBookingStateValue(String state) {
        if (Arrays.stream(BookingStateDto.values()).map(Enum::toString).noneMatch(e -> e.equals(state))) {
            throw new IllegalArgumentException("Unknown state: " + state);
        } else {
            return BookingStateDto.valueOf(state);
        }
    }
}
