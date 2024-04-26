package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingMapperTest {
    @Mock
    private UserMapper userMapper;
    @Mock
    private ItemMapper itemMapper;
    @InjectMocks
    private BookingMapper bookingMapper;
    private Booking booking;
    private BookingDto bookingDto;
    private BookingShortDto bookingShortDto;
    private User user;
    private UserDto userDto;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1)
                .name("test user")
                .email("test@user.email")
                .build();
        userDto = UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
        item = Item.builder()
                .id(1)
                .name("test item")
                .description("test description")
                .owner(user)
                .available(true)
                .requestId(1)
                .build();
        itemDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .owner(userDto)
                .requestId(item.getRequestId())
                .available(item.getAvailable())
                .build();
        booking = Booking.builder()
                .id(1)
                .status(BookingState.APPROVED)
                .booker(user)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .item(item)
                .build();
        bookingDto = BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStartDate())
                .end(booking.getEndDate())
                .booker(userDto)
                .status(BookingStateDto.APPROVED)
                .item(itemDto)
                .build();
        bookingShortDto = BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    @Test
    void convertBooking_whenSuccessful_thenReturnBookingDto() {
        when(itemMapper.convertItem(item)).thenReturn(itemDto);
        when(userMapper.convertUser(user)).thenReturn(userDto);

        BookingDto actualBookingDto = bookingMapper.convertBooking(booking);

        assertThat(bookingDto, is(actualBookingDto));
    }

    @Test
    void convertBookingToShortDto_whenSuccessful_thenReturnBookingShortDto() {
        BookingShortDto actualBookingShortDto = bookingMapper.convertBookingToShortDto(booking);

        assertThat(bookingShortDto, is(actualBookingShortDto));
    }
}
