package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ItemMapper itemMapper;
    @InjectMocks
    private CommentServiceImpl commentService;

    private User user;
    private User user2;
    private UserDto user2Dto;
    private Item item;
    private CommentCreateDto commentCreateDto;
    private CommentDto commentDto;
    private CommentDto commentDtoSaved;
    private Comment comment;
    private Booking booking;
    private UserDto userDto;
    private ItemDto itemDto;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1)
                .name("test user")
                .email("test@user.email")
                .build();
        user2 = User.builder()
                .id(2)
                .name("test user2")
                .email("test@user2.email")
                .build();
        userDto = UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
        user2Dto = UserDto.builder()
                .id(user2.getId())
                .name(user2.getName())
                .email(user2.getEmail())
                .build();
        item = Item.builder()
                .id(1)
                .owner(user)
                .available(true)
                .description("item description")
                .name("item name")
                .build();
        itemDto = ItemDto.builder()
                .id(item.getId())
                .description(item.getDescription())
                .available(item.getAvailable())
                .name(item.getName())
                .build();
        commentCreateDto = CommentCreateDto.builder()
                .text("comment text")
                .authorId(user2.getId())
                .itemId(item.getId())
                .created(LocalDateTime.now())
                .build();
        commentDto = CommentDto.builder()
                .text(commentCreateDto.getText())
                .author(user2Dto)
                .item(itemDto)
                .created(commentCreateDto.getCreated())
                .build();
        commentDtoSaved = CommentDto.builder()
                .id(1)
                .text(commentCreateDto.getText())
                .authorName(user2.getName())
                .author(user2Dto)
                .item(itemDto)
                .created(commentCreateDto.getCreated())
                .build();
        comment = Comment.builder()
                .id(commentDto.getId())
                .item(item)
                .text(commentDto.getText())
                .created(commentDto.getCreated())
                .author(user2)
                .build();
        booking = Booking.builder()
                .id(1)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .status(BookingState.APPROVED)
                .booker(user2)
                .item(item)
                .build();
    }

    @Test
    void create_whenSuccessful_thenReturnCommentDto() {
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByBookerIdAndItemIdAndStatusAndEndDateIsBefore(user2.getId(),
                item.getId(), BookingState.APPROVED, commentCreateDto.getCreated()))
                .thenReturn(Optional.of(booking));
        when(userMapper.convertUser(user2)).thenReturn(user2Dto);
        when(itemMapper.convertItem(item)).thenReturn(itemDto);
        when(commentMapper.convertDto(commentDto)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.convertComment(comment)).thenReturn(commentDtoSaved);

        CommentDto actualCommentDto = commentService.create(commentCreateDto);

        assertThat(commentDtoSaved, is(actualCommentDto));
        verify(userRepository, times(1)).findById(user2.getId());
        verify(itemRepository, times(1)).findById(item.getId());
        verify(bookingRepository, times(1))
                .findFirstByBookerIdAndItemIdAndStatusAndEndDateIsBefore(user2.getId(),
                item.getId(), BookingState.APPROVED, commentCreateDto.getCreated());
        verify(userMapper, times(1)).convertUser(user2);
        verify(itemMapper, times(1)).convertItem(item);
        verify(commentMapper, times(1)).convertDto(commentDto);
        verify(commentRepository, times(1)).save(comment);
        verify(commentMapper, times(1)).convertComment(comment);
    }

    @Test
    void create_whenUserNotFound_thenThrownException() {
        when(userRepository.findById(user2.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.create(commentCreateDto));
        assertThat(exception.getMessage(),
                is("Пользователя с id " + user2.getId() + " не существует"));
        verify(userRepository, times(1)).findById(user2.getId());
        verify(itemRepository, never()).findById(anyInt());
        verify(bookingRepository, never())
                .findFirstByBookerIdAndItemIdAndStatusAndEndDateIsBefore(anyInt(),
                        anyInt(), any(BookingState.class), any(LocalDateTime.class));
        verify(userMapper, never()).convertUser(any(User.class));
        verify(itemMapper, never()).convertItem(any(Item.class));
        verify(commentMapper, never()).convertDto(any(CommentDto.class));
        verify(commentRepository, never()).save(any(Comment.class));
        verify(commentMapper, never()).convertComment(any(Comment.class));
    }

    @Test
    void create_whenItemNotFound_thenThrownException() {
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> commentService.create(commentCreateDto));
        assertThat(exception.getMessage(),
                is("Вещи с id " + item.getId() + " не существует"));
        verify(userRepository, times(1)).findById(user2.getId());
        verify(itemRepository, times(1)).findById(item.getId());
        verify(bookingRepository, never())
                .findFirstByBookerIdAndItemIdAndStatusAndEndDateIsBefore(anyInt(),
                        anyInt(), any(BookingState.class), any(LocalDateTime.class));
        verify(userMapper, never()).convertUser(any(User.class));
        verify(itemMapper, never()).convertItem(any(Item.class));
        verify(commentMapper, never()).convertDto(any(CommentDto.class));
        verify(commentRepository, never()).save(any(Comment.class));
        verify(commentMapper, never()).convertComment(any(Comment.class));
    }

    @Test
    void create_whenUserDidNotBookedItem_thenThrownException() {
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findFirstByBookerIdAndItemIdAndStatusAndEndDateIsBefore(user2.getId(),
                item.getId(), BookingState.APPROVED, commentCreateDto.getCreated()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> commentService.create(commentCreateDto));
        assertThat(exception.getMessage(),
                is("Пользователь " + user2
                        + " не брал вещь с id " + item.getId()));
        verify(userRepository, times(1)).findById(user2.getId());
        verify(itemRepository, times(1)).findById(item.getId());
        verify(bookingRepository, times(1))
                .findFirstByBookerIdAndItemIdAndStatusAndEndDateIsBefore(user2.getId(),
                        item.getId(), BookingState.APPROVED, commentCreateDto.getCreated());
        verify(userMapper, never()).convertUser(any(User.class));
        verify(itemMapper, never()).convertItem(any(Item.class));
        verify(commentMapper, never()).convertDto(any(CommentDto.class));
        verify(commentRepository, never()).save(any(Comment.class));
        verify(commentMapper, never()).convertComment(any(Comment.class));
    }
}
