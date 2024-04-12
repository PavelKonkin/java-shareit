package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

@Component
public class BookingMapper {
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    @Autowired
    public BookingMapper(UserMapper userMapper, ItemMapper itemMapper) {
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
    }

    public Booking convertDto(BookingDto bookingDto) {
        return Booking.builder()
                .id(bookingDto.getId())
                .startDate(bookingDto.getStart())
                .endDate(bookingDto.getEnd())
                .status(BookingState.valueOf(bookingDto.getStatus().toString()))
                .item(itemMapper.convertItemDto(bookingDto.getItem()))
                .booker(userMapper.convertUserDto(bookingDto.getBooker()))
                .build();
    }

    public BookingDto convertBooking(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStartDate())
                .end(booking.getEndDate())
                .status(BookingStateDto.valueOf(booking.getStatus().toString()))
                .item(itemMapper.convertItem(booking.getItem()))
                .booker(userMapper.convertUser(booking.getBooker()))
                .build();
    }

    public BookingShortDto convertBookingToShortDto(Booking booking) {
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }
}
