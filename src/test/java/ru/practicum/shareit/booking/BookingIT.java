package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
public class BookingIT {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingMapper bookingMapper;
    @Autowired
    private BookingService bookingService;

    private User user;
    private User user2;
    private Item item;
    private Item item2;
    private BookingCreateDto bookingCreateDto;
    private Booking booking;
    private Booking bookingApproved;

    @BeforeEach
    void setup() {
        user = User.builder()
                .name("test user")
                .email("test@user.email")
                .build();
        userRepository.save(user);
        user2 = User.builder()
                .name("test user2")
                .email("test@user.email2")
                .build();
        userRepository.save(user2);
        item = Item.builder()
                .name("test item")
                .description("test description")
                .owner(user)
                .requestId(1)
                .available(true)
                .build();
        itemRepository.save(item);
        item2 = Item.builder()
                .name("test item2")
                .description("test description2")
                .owner(user)
                .requestId(1)
                .available(false)
                .build();
        itemRepository.save(item2);
        bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .bookerId(user2.getId())
                .status(BookingStateDto.WAITING)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();
        booking = Booking.builder()
                .booker(user2)
                .item(item)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .status(BookingState.WAITING)
                .build();
        bookingApproved = Booking.builder()
                .booker(user2)
                .item(item)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .status(BookingState.APPROVED)
                .build();
        bookingRepository.save(booking);
        bookingRepository.save(bookingApproved);
    }

    @Test
    void create_whenSuccessful_thenReturnBookingDto() {
        BookingDto actualBookingDto = bookingService.create(bookingCreateDto);

        assertThat(actualBookingDto, notNullValue());
        assertThat(actualBookingDto.getId(), notNullValue());
        assertThat(actualBookingDto.getItem().getName(), is(item.getName()));
        assertThat(actualBookingDto.getItem().getDescription(), is(item.getDescription()));
        assertThat(actualBookingDto.getBooker().getName(), is(user2.getName()));
    }

    @Test
    void create_whenUserNotFound_thenThrownException() {
        int wrongUserId = 66;
        bookingCreateDto.setBookerId(wrongUserId);

        assertThrows(NotFoundException.class,
                () -> bookingService.create(bookingCreateDto));
    }

    @Test
    void create_whenItemNotFound_thenThrownException() {
        int wrongItemId = 66;
        bookingCreateDto.setItemId(wrongItemId);

        assertThrows(NotFoundException.class,
                () -> bookingService.create(bookingCreateDto));
    }

    @Test
    void create_whenBookerIsOwner_thenThrownException() {
        bookingCreateDto.setBookerId(user.getId());

        assertThrows(NotFoundException.class,
                () -> bookingService.create(bookingCreateDto));
    }

    @Test
    void create_whenItemNotAvailable_thenThrownException() {
        bookingCreateDto.setItemId(item2.getId());

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(bookingCreateDto));
    }

    @Test
    void confirmReject_whenApproved_thenReturnApprovedBookingDto() {
        BookingDto actualBookingDto
                = bookingService.confirmReject(user.getId(), booking.getId(), "true");

        assertThat(actualBookingDto, notNullValue());
        assertThat(actualBookingDto.getId(), is(booking.getId()));
        assertThat(actualBookingDto.getStatus(), is(BookingStateDto.APPROVED));
    }

    @Test
    void confirmReject_whenRejected_thenReturnRejectedBookingDto() {
        BookingDto actualBookingDto
                = bookingService.confirmReject(user.getId(), booking.getId(), "false");

        assertThat(actualBookingDto, notNullValue());
        assertThat(actualBookingDto.getId(), is(booking.getId()));
        assertThat(actualBookingDto.getStatus(), is(BookingStateDto.REJECTED));
    }

    @Test
    void confirmReject_whenBookingNotFound_thenThrownException() {
        int wrongBookingId = 66;

        assertThrows(NotFoundException.class,
                () -> bookingService.confirmReject(user.getId(), wrongBookingId, "false"));
    }

    @Test
    void confirmReject_whenUserNotOwner_thenThrownException() {
        int wrongUserId = 66;

        assertThrows(NotFoundException.class,
                () -> bookingService.confirmReject(wrongUserId, booking.getId(), "false"));
    }

    @Test
    void confirmReject_whenBookingAlreadyApproved_thenThrownException() {
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.confirmReject(user.getId(), bookingApproved.getId(), "false"));
    }

    @Test
    void get_whenSuccessful_thenReturnBookingDto() {
        BookingDto actualBookingDto = bookingService.get(user.getId(), booking.getId());

        assertThat(actualBookingDto.getId(), is(booking.getId()));
        assertThat(actualBookingDto.getBooker().getId(), is(user2.getId()));
        assertThat(actualBookingDto.getItem().getId(), is(item.getId()));
    }

    @Test
    void get_whenBookingNotFound_thenThrownException() {
        int wrongBookingId = 66;

        assertThrows(NotFoundException.class,
                () -> bookingService.get(user.getId(), wrongBookingId));
    }

    @Test
    void getAllForBooker_whenWaiting_thenReturnListOfBookingDto() {
        List<BookingDto> actualListOfBookingsDto
                = bookingService.getAllForBooker(user2.getId(), BookingStateDto.WAITING, 0, 10);

        assertThat(actualListOfBookingsDto, iterableWithSize(1));
        assertThat(actualListOfBookingsDto.get(0).getId(), is(booking.getId()));
        assertThat(actualListOfBookingsDto.get(0).getItem().getId(), is(booking.getItem().getId()));
        assertThat(actualListOfBookingsDto.get(0).getBooker().getId(), is(booking.getBooker().getId()));
        assertThat(actualListOfBookingsDto.get(0).getStatus(), is(BookingStateDto.WAITING));
    }

    @Test
    void getAllForBooker_whenUserNotFound_thenThrownException() {
        int wrongUserId = 66;

        assertThrows(NotFoundException.class,
                () -> bookingService.getAllForBooker(wrongUserId, BookingStateDto.WAITING, 0, 10));
    }

    @Test
    void getAllForOwner_whenWaiting_thenReturnListOfBookingDto() {
        List<BookingDto> actualListOfBookingsDto
                = bookingService.getAllForOwner(user.getId(), BookingStateDto.WAITING, 0, 10);

        assertThat(actualListOfBookingsDto, iterableWithSize(1));
        assertThat(actualListOfBookingsDto.get(0).getId(), is(booking.getId()));
        assertThat(actualListOfBookingsDto.get(0).getItem().getId(), is(booking.getItem().getId()));
        assertThat(actualListOfBookingsDto.get(0).getBooker().getId(), is(booking.getBooker().getId()));
        assertThat(actualListOfBookingsDto.get(0).getStatus(), is(BookingStateDto.WAITING));
    }

    @Test
    void getAllForOwner_whenUserNotFound_thenThrownException() {
        int wrongUserId = Integer.MAX_VALUE;

        assertThrows(NotFoundException.class,
                () -> bookingService.getAllForOwner(wrongUserId, BookingStateDto.WAITING, 0, 10));
    }


}
