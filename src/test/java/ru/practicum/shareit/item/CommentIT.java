package ru.practicum.shareit.item;

import org.hibernate.service.spi.InjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
public class CommentIT {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private CommentService commentService;

    private CommentCreateDto commentCreateDto;
    private User user;
    private User user2;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setup() {
        user = User.builder()
                .name("test user")
                .email("test@user.email")
                .build();
        user2 = User.builder()
                .name("test user2")
                .email("test@user2.email")
                .build();
        userRepository.save(user);
        userRepository.save(user2);
        item = Item.builder()
                .owner(user)
                .requestId(1)
                .name("test item")
                .description("test description")
                .available(true)
                .build();
        itemRepository.save(item);
        booking = Booking.builder()
                .booker(user2)
                .status(BookingState.APPROVED)
                .item(item)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().minusHours(1))
                .build();
        bookingRepository.save(booking);
        commentCreateDto = CommentCreateDto.builder()
                .text("comment text")
                .created(LocalDateTime.now())
                .itemId(item.getId())
                .authorId(user2.getId())
                .build();
    }


    @Test
    void create_whenSuccessful_thenReturnCommentDto() {
        CommentDto actualCommentDto = commentService.create(commentCreateDto);

        assertThat(actualCommentDto, instanceOf(CommentDto.class));
        assertThat(actualCommentDto.getAuthorName(), is(user2.getName()));
        assertThat(actualCommentDto.getText(), is(commentCreateDto.getText()));
        assertThat(actualCommentDto.getCreated(), is(commentCreateDto.getCreated()));
        assertThat(actualCommentDto.getId(), notNullValue());
    }

    @Test
    void create_whenUserNotFound_thenThrownException() {
        commentCreateDto.setAuthorId(66);
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.create(commentCreateDto));
        assertThat(exception.getMessage(), is("Пользователя с id " + 66 + " не существует"));

    }

    @Test
    void create_whenItemNotFound_thenThrownException() {
        commentCreateDto.setItemId(66);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> commentService.create(commentCreateDto));
        assertThat(exception.getMessage(), is("Вещи с id " + 66 + " не существует"));
    }

    @Test
    void create_whenUserDidNotBooked_thenThrownException() {
        commentCreateDto.setAuthorId(user.getId());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> commentService.create(commentCreateDto));
        assertThat(exception.getMessage(), is("Пользователь " + user
                + " не брал вещь с id " + item.getId()));

    }
}
