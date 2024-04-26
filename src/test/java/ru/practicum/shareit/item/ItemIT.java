package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
public class ItemIT {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private BookingMapper bookingMapper;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private ItemService itemService;

    private User user1;
    private User user2;
    private Item item;
    private ItemDto itemDto;
    private Booking booking;
    private Comment comment;
    private ItemDto itemUpdateDto;

    @BeforeEach
    void setup() {
        user1 = User.builder()
                .name("test user")
                .email("test@user.email")
                .build();
        user2 = User.builder()
                .name("test user2")
                .email("test@user2.email")
                .build();
        userRepository.save(user1);
        userRepository.save(user2);
        item = Item.builder()
                .name("test item")
                .description("test description")
                .available(true)
                .owner(user1)
                .requestId(1)
                .build();
        itemRepository.save(item);
        booking = Booking.builder()
                .item(item)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .booker(user2)
                .status(BookingState.APPROVED)
                .build();
        bookingRepository.save(booking);
        comment = Comment.builder()
                .text("test comment")
                .author(user2)
                .item(item)
                .created(LocalDateTime.now())
                .build();
        commentRepository.save(comment);
        itemDto = ItemDto.builder()
                .available(true)
                .name("test item 2")
                .description("test description 2")
                .requestId(1)
                .build();
        itemUpdateDto = ItemDto.builder()
                .id(item.getId())
                .name("update name")
                .description("update description")
                .build();
    }

    @Test
    void getAll_whenSuccessful_thenReturnListOfItemDtos() {
        List<ItemWithBookingsAndCommentsDto> actualListOfItemDtos
                = itemService.getAll(user1.getId(), 0, 10);

        assertThat(actualListOfItemDtos, iterableWithSize(1));
        assertThat(actualListOfItemDtos.get(0).getComments(), contains(commentMapper.convertComment(comment)));
        assertThat(actualListOfItemDtos.get(0).getLastBooking(), is(bookingMapper.convertBookingToShortDto(booking)));
        assertThat(actualListOfItemDtos.get(0).getNextBooking(), is(nullValue()));
        assertThat(actualListOfItemDtos.get(0).getOwner(), is(userMapper.convertUser(user1)));
    }

    @Test
    void getAll_whenUserHasNoItems_thenReturnEmptyList() {
        List<ItemWithBookingsAndCommentsDto> actualListOfItemDtos
                = itemService.getAll(user2.getId(), 0, 10);

        assertThat(actualListOfItemDtos, emptyIterable());
    }

    @Test
    void create_whenSuccessful_thenReturnItemDto() {
        ItemDto actualItemDto = itemService.create(itemDto, user2.getId());

        assertThat(actualItemDto, notNullValue());
        assertThat(actualItemDto.getId(), notNullValue());
        assertThat(actualItemDto.getName(), is(itemDto.getName()));
        assertThat(actualItemDto.getDescription(), is(itemDto.getDescription()));
        assertThat(actualItemDto.getAvailable(), is(itemDto.getAvailable()));
        assertThat(actualItemDto.getRequestId(), is(itemDto.getRequestId()));
    }

    @Test
    void create_whenUserNotFound_thenThrownException() {
        assertThrows(NotFoundException.class,
                () -> itemService.create(itemDto, 66));
    }

    @Test
    void update_whenSuccessful_thenReturnItemDto() {
        ItemDto actualItemDto = itemService.update(itemUpdateDto, user1.getId());

        assertThat(actualItemDto.getName(), is(itemUpdateDto.getName()));
        assertThat(actualItemDto.getDescription(), is(itemUpdateDto.getDescription()));
        assertThat(actualItemDto.getOwner().getId(), is(user1.getId()));
        assertThat(actualItemDto.getRequestId(), is(item.getRequestId()));
    }

    @Test
    void update_whenUserNotOwner_thenThrownException() {
        assertThrows(NotFoundException.class,
                () -> itemService.update(itemUpdateDto, user2.getId()));
    }

    @Test
    void update_whenUserNotFound_thenThrownException() {
        assertThrows(NotFoundException.class,
                () -> itemService.update(itemUpdateDto, 66));
    }

    @Test
    void update_whenItemNotFound_thenThrownException() {
        itemUpdateDto.setId(66);
        assertThrows(NotFoundException.class,
                () -> itemService.update(itemUpdateDto, user1.getId()));
    }

    @Test
    void get_whenSuccessful_thenReturnItemWithBookingAndCommentsDto() {
        ItemWithBookingsAndCommentsDto actualItem = itemService.get(item.getId(), user1.getId());

        assertThat(actualItem, notNullValue());
        assertThat(actualItem.getOwner(), is(userMapper.convertUser(user1)));
        assertThat(actualItem.getLastBooking(), is(bookingMapper.convertBookingToShortDto(booking)));
        assertThat(actualItem.getNextBooking(), nullValue());
        assertThat(actualItem.getComments(), contains(commentMapper.convertComment(comment)));
    }

    @Test
    void get_whenUserNotOwner_thenReturnItemWithoutBookings() {
        ItemWithBookingsAndCommentsDto actualItem = itemService.get(item.getId(), user2.getId());

        assertThat(actualItem, notNullValue());
        assertThat(actualItem.getOwner(), is(userMapper.convertUser(user1)));
        assertThat(actualItem.getLastBooking(), nullValue());
        assertThat(actualItem.getNextBooking(), nullValue());
        assertThat(actualItem.getComments(), contains(commentMapper.convertComment(comment)));
    }

    @Test
    void get_whenItemNotFound_thenThrownException() {
        assertThrows(NotFoundException.class,
                () -> itemService.get(item.getId() + 1, user1.getId()));
    }

    @Test
    void search_whenSuccessful_thenReturnListOfItemDtos() {
        String text = "test";

        List<ItemDto> actualListOfItemDtos = itemService.search(text, 0, 10);

        assertThat(actualListOfItemDtos, iterableWithSize(1));
        assertThat(actualListOfItemDtos, contains(itemMapper.convertItem(item)));
    }

    @Test
    void search_whenSearchTextEmpty_thenReturnEmptyList() {
        String text = "";

        List<ItemDto> actualListOfItemDtos = itemService.search(text, 0, 10);

        assertThat(actualListOfItemDtos, iterableWithSize(0));
    }
}
