package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Transactional
@DataJpaTest
public class BookingRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private User user;
    private User user2;
    private Item item;
    private Item item2;
    private Booking booking;
    private Booking booking2;
    private final Sort sort = Sort.by("id").descending();
    private final PageRequest page = PageRequest.of(0, 10, sort);

    @BeforeEach
    void setup() {
        user = User.builder()
                .name("test user")
                .email("test@user.email")
                .build();
        userRepository.save(user);
        user2 = User.builder()
                .name("test user2")
                .email("test@user2.email")
                .build();
        userRepository.save(user2);
        item = Item.builder()
                .available(true)
                .owner(user)
                .requestId(1)
                .name("test item")
                .description("test description")
                .build();
        itemRepository.save(item);
        item2 = Item.builder()
                .available(true)
                .owner(user2)
                .requestId(1)
                .name("test item2")
                .description("test description2")
                .build();
        itemRepository.save(item2);
        booking = Booking.builder()
                .booker(user2)
                .status(BookingState.APPROVED)
                .item(item)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        bookingRepository.save(booking);
        booking2 = Booking.builder()
                .booker(user)
                .status(BookingState.APPROVED)
                .item(item2)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        bookingRepository.save(booking2);
    }

    @Test
    void getByIdAndBookerIdOrItemOwnerId_whenFound_thenReturnOptionalOfBooking() {
        Optional<Booking> actualBookingOptional
                = bookingRepository.getByIdAndBookerIdOrItemOwnerId(booking.getId(), user.getId());

        assertThat(Optional.of(booking), is(actualBookingOptional));
    }

    @Test
    void findAllByBookerId_whenFound_thenReturnPageOfBookings() {
        Page<Booking> actualPageOfBookings = bookingRepository.findAllByBookerId(user2.getId(), page);

        assertThat(List.of(booking), is(actualPageOfBookings.toList()));
    }

    @Test
    void findAllByBookerIdAndStartDateAfter_whenFound_thenReturnOptionalOfBooking() {
        Page<Booking> actualPageOfBookings = bookingRepository
                .findAllByBookerIdAndStartDateAfter(user2.getId(), LocalDateTime.now().minusDays(2), page);

        assertThat(List.of(booking), is(actualPageOfBookings.toList()));
    }

    @Test
    void findAllByBookerIdAndEndDateIsBefore_whenFound_thenReturnOptionalOfBooking() {
        Page<Booking> actualPageOfBookings = bookingRepository
                .findAllByBookerIdAndEndDateIsBefore(user2.getId(), LocalDateTime.now().plusDays(2), page);

        assertThat(List.of(booking), is(actualPageOfBookings.toList()));
    }

    @Test
    void findAllByBookerCurrent_whenFound_thenReturnOptionalOfBooking() {
        Page<Booking> actualPageOfBookings = bookingRepository
                .findAllByBookerCurrent(user2.getId(), LocalDateTime.now(), page);

        assertThat(List.of(booking), is(actualPageOfBookings.toList()));
    }

    @Test
    void findAllByBookerIdAndStatus_whenFound_thenReturnOptionalOfBooking() {
        Page<Booking> actualPageOfBookings = bookingRepository
                .findAllByBookerIdAndStatus(user2.getId(), BookingState.APPROVED, page);

        assertThat(List.of(booking), is(actualPageOfBookings.toList()));
    }

    @Test
    void findAllByItemOwnerId_whenFound_thenReturnOptionalOfBooking() {
        Page<Booking> actualPageOfBookings = bookingRepository
                .findAllByItemOwnerId(user2.getId(), page);

        assertThat(List.of(booking2), is(actualPageOfBookings.toList()));
    }

    @Test
    void findAllByItemOwnerIdAndStartDateAfter_whenFound_thenReturnOptionalOfBooking() {
        Page<Booking> actualPageOfBookings = bookingRepository
                .findAllByItemOwnerIdAndStartDateAfter(user2.getId(), LocalDateTime.now().minusDays(2), page);

        assertThat(List.of(booking2), is(actualPageOfBookings.toList()));
    }

    @Test
    void findAllByItemOwnerIdAndEndDateIsBefore_whenFound_thenReturnOptionalOfBooking() {
        Page<Booking> actualPageOfBookings = bookingRepository
                .findAllByItemOwnerIdAndEndDateIsBefore(user2.getId(), LocalDateTime.now().plusDays(2), page);

        assertThat(List.of(booking2), is(actualPageOfBookings.toList()));
    }

    @Test
    void findAllByOwnerCurrent_whenFound_thenReturnOptionalOfBooking() {
        Page<Booking> actualPageOfBookings = bookingRepository
                .findAllByOwnerCurrent(user2.getId(), LocalDateTime.now(), page);

        assertThat(List.of(booking2), is(actualPageOfBookings.toList()));
    }

    @Test
    void findAllByItemOwnerIdAndStatus_whenFound_thenReturnOptionalOfBooking() {
        Page<Booking> actualPageOfBookings = bookingRepository
                .findAllByItemOwnerIdAndStatus(user2.getId(), BookingState.APPROVED, page);

        assertThat(List.of(booking2), is(actualPageOfBookings.toList()));
    }

    @Test
    void findAllByItemId_whenFound_thenReturnOptionalOfBooking() {
        List<Booking> actualListOfBookings = bookingRepository
                .findAllByItemId(item.getId());

        assertThat(List.of(booking), is(actualListOfBookings));
    }

    @Test
    void findFirstByBookerIdAndItemIdAndStatusAndEndDateIsBefore_whenFound_thenReturnOptionalOfBooking() {
        Optional<Booking> actualOptionalOfBookings = bookingRepository
                .findFirstByBookerIdAndItemIdAndStatusAndEndDateIsBefore(user2.getId(), item.getId(),
                        BookingState.APPROVED, LocalDateTime.now().plusDays(2));

        assertThat(Optional.of(booking), is(actualOptionalOfBookings));
    }

    @Test
    void findAllByItemsId_whenFound_thenReturnListOfBooking() {
        List<Booking> actualListOfBookings = bookingRepository
                .findAllByItemsId(List.of(item.getId()));

        assertThat(List.of(booking), is(actualListOfBookings));
    }
}
