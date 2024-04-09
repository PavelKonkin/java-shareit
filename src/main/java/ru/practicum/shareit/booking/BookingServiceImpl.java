package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository,
                              ItemRepository itemRepository, BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bookingMapper = bookingMapper;
    }

    @Override
    public BookingDto create(BookingCreateDto bookingCreateDto) {
        int userId = bookingCreateDto.getBookerId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует"));
        int itemId = bookingCreateDto.getItemId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("вещи с id " + itemId + " не существует"));
        if (item.getOwner().getId() == userId) {
            throw new NotFoundException("Владелец вещи не может брать у себя её в аренду");
        }
        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь " + item + " недоступна для бронирования");
        }
        Booking booking = Booking.builder()
                .endDate(bookingCreateDto.getEnd())
                .startDate(bookingCreateDto.getStart())
                .item(item)
                .booker(user)
                .status(BookingState.WAITING)
                .build();

        return bookingMapper.convertBooking(bookingRepository.save(booking));
    }

    @Override
    public BookingDto confirmReject(int userId, int bookingId, String approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирования с id " + bookingId + " не существует"));
        int ownerId = booking.getItem().getOwner().getId();
        if (ownerId != userId) {
            throw new NotFoundException("Попытка изменить статус бронирования вещи с id "
                    + booking.getItem().getId() + " пользователем с id " + userId +
                    ", не являющимся владельцем вещи ");
        }
        if (booking.getStatus() != BookingState.WAITING) {
            throw new IllegalArgumentException("Попытка повторного подтверждения или отказа в бронировании "
                    + booking);
        }
        BookingState newState;
        if (approved.equals("true")) {
            newState = BookingState.APPROVED;
        } else {
            newState = BookingState.REJECTED;
        }

        Booking updatedStateBooking = booking.toBuilder()
                .status(newState)
                .build();

        return bookingMapper.convertBooking(bookingRepository.save(updatedStateBooking));
    }

    @Override
    public BookingDto get(int userId, int bookingId) {
        Booking booking = bookingRepository.getByIdAndBookerIdOrItemOwnerId(bookingId, userId)
                .orElseThrow(() -> new NotFoundException("Бронирования с id " + bookingId + " с создателем с id "
                + userId + " или id владельца вещи " + userId + " не найдено"));
        return bookingMapper.convertBooking(booking);
    }

    @Override
    public List<BookingDto> getAllForBooker(int userId, String state) {
        checkUser(userId);
        BookingStateDto bookingStateDto = getBookingStateDto(state);
        List<Booking> result = new ArrayList<>();

        switch (bookingStateDto) {
            case ALL:
                result = bookingRepository.findAllByBookerIdOrderByIdDesc(userId);
                break;
            case FUTURE:
                result = bookingRepository.findAllByBookerIdAndStartDateAfterOrderByIdDesc(userId, LocalDateTime.now());
                break;
            case PAST:
                result = bookingRepository.findAllByBookerIdAndEndDateIsBeforeOrderByIdDesc(userId, LocalDateTime.now());
                break;
            case REJECTED:
                result = bookingRepository.findAllByBookerIdAndStatusOrderByIdDesc(userId, BookingState.REJECTED);
                break;
            case APPROVED:
                result = bookingRepository.findAllByBookerIdAndStatusOrderByIdDesc(userId, BookingState.APPROVED);
                break;
            case WAITING:
                result = bookingRepository.findAllByBookerIdAndStatusOrderByIdDesc(userId, BookingState.WAITING);
                break;
            case CURRENT:
                result = bookingRepository.findAllByBookerCurrent(userId, LocalDateTime.now());
        }
        return result.stream()
                .map(bookingMapper::convertBooking)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllForOwner(int userId, String state) {
        checkUser(userId);
        BookingStateDto bookingStateDto = getBookingStateDto(state);
        List<Booking> result = new ArrayList<>();

        switch (bookingStateDto) {
            case ALL:
                result = bookingRepository.findAllByItemOwnerIdOrderByIdDesc(userId);
                break;
            case FUTURE:
                result = bookingRepository.findAllByItemOwnerIdAndStartDateAfterOrderByIdDesc(userId,
                        LocalDateTime.now());
                break;
            case PAST:
                result = bookingRepository.findAllByItemOwnerIdAndEndDateIsBeforeOrderByIdDesc(userId,
                        LocalDateTime.now());
                break;
            case REJECTED:
                result = bookingRepository.findAllByItemOwnerIdAndStatusOrderByIdDesc(userId, BookingState.REJECTED);
                break;
            case APPROVED:
                result = bookingRepository.findAllByItemOwnerIdAndStatusOrderByIdDesc(userId, BookingState.APPROVED);
                break;
            case WAITING:
                result = bookingRepository.findAllByItemOwnerIdAndStatusOrderByIdDesc(userId, BookingState.WAITING);
                break;
            case CURRENT:
                result = bookingRepository.findAllByOwnerCurrent(userId, LocalDateTime.now());
        }
        return result.stream()
                .map(bookingMapper::convertBooking)
                .collect(Collectors.toList());
    }

    private void checkUser(int userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id "
                        + userId + " не существует"));
    }

    private BookingStateDto getBookingStateDto(String state) {
        BookingStateDto bookingStateDto;
        try {
            bookingStateDto = BookingStateDto.valueOf(state);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
        return bookingStateDto;
    }
}