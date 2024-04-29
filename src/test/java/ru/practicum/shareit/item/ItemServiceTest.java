package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.page.OffsetPage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private  ItemRepository itemRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper commentMapper;
    @InjectMocks
    private ItemServiceImpl itemService;

    private final Sort sort = Sort.by("id");
    private final Pageable page = new OffsetPage(0, 10, sort);

    private ItemDto itemDto;
    private ItemDto updateItemDto;
    private ItemDto wrongUpdateItemDto;
    private ItemDto savedItemDto;
    private ItemWithBookingsAndCommentsDto itemWithBookingsAndCommentsDtoConverted;
    private ItemWithBookingsAndCommentsDto itemWithBookingsAndCommentsDto;
    private Item newItem;
    private Item savedItem;
    private Item updateItem;
    private Item updateItemToSave;
    private User user;
    private User user2;
    private UserDto userDto;
    private UserDto userDto2;
    private UserDto wrongUserDto;
    private Booking booking;
    private Booking booking2;
    private BookingShortDto bookingShortDto;
    private BookingShortDto bookingShortDto2;
    private Comment comment;
    private CommentDto commentDto;

    @BeforeEach
    void setup() {
        itemDto = ItemDto.builder()
                .name("test item")
                .description("test description")
                .available(true)
                .build();
        updateItemDto = ItemDto.builder()
                .id(1)
                .name("update test item")
                .description("update test description")
                .available(true)
                .build();
        wrongUpdateItemDto = updateItemDto.toBuilder()
                .id(66).build();
        newItem = Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
        user = User.builder()
                .id(1)
                .name("test user")
                .email("test@user.email")
                .build();
        user2 = user.toBuilder()
                .id(2)
                .name("test user 2")
                .build();
        savedItem = newItem.toBuilder()
                .id(1)
                .owner(user)
                .build();
        updateItem = Item.builder()
                .id(updateItemDto.getId())
                .name(savedItem.getName())
                .description(savedItem.getDescription())
                .owner(savedItem.getOwner())
                .available(savedItem.getAvailable())
                .build();
        updateItemToSave = updateItem.toBuilder()
                .name(updateItemDto.getName())
                .description(updateItemDto.getDescription())
                .build();
        userDto = UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
        userDto2 = UserDto.builder()
                .id(user2.getId())
                .name(user2.getName())
                .email(user2.getEmail())
                .build();
        wrongUserDto = userDto.toBuilder().id(66).build();
        savedItemDto = itemDto.toBuilder()
                .id(savedItem.getId())
                .build();
        booking = Booking.builder()
                .id(1)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .item(savedItem)
                .status(BookingState.APPROVED)
                .booker(user)
                .build();
        booking2 = Booking.builder()
                .id(2)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .item(savedItem)
                .status(BookingState.APPROVED)
                .booker(user)
                .build();
        bookingShortDto = BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
        bookingShortDto2 = BookingShortDto.builder()
                .id(booking2.getId())
                .bookerId(booking2.getBooker().getId())
                .build();
        comment = Comment.builder()
                .id(1)
                .text("text")
                .author(user2)
                .item(savedItem)
                .created(LocalDateTime.now().minusHours(12))
                .build();
        commentDto = CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .item(savedItemDto)
                .created(comment.getCreated())
                .authorName(comment.getAuthor().getName())
                .author(userDto2)
                .build();
        itemWithBookingsAndCommentsDto = ItemWithBookingsAndCommentsDto.builder()
                .id(savedItem.getId())
                .name(savedItem.getName())
                .description(savedItem.getDescription())
                .available(savedItem.getAvailable())
                .lastBooking(bookingShortDto)
                .nextBooking(bookingShortDto2)
                .comments(List.of(commentDto))
                .build();
        itemWithBookingsAndCommentsDtoConverted = ItemWithBookingsAndCommentsDto.builder()
                .id(savedItem.getId())
                .name(savedItem.getName())
                .description(savedItem.getDescription())
                .available(savedItem.getAvailable())
                .build();
    }

    @Test
    void create_whenSuccessful_thenReturnItemDto() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemMapper.convertItemDto(itemDto)).thenReturn(newItem);
        when(itemRepository.save(newItem)).thenReturn(savedItem);
        when(itemMapper.convertItem(savedItem)).thenReturn(savedItemDto);

        ItemDto actualItemDto = itemService.create(itemDto, user.getId());

        assertThat(savedItemDto, is(actualItemDto));
        verify(userRepository, times(1)).findById(user.getId());
        verify(itemMapper, times(1)).convertItemDto(itemDto);
        verify(itemRepository, times(1)).save(newItem);
        verify(itemMapper, times(1)).convertItem(savedItem);
    }

    @Test
    void create_whenUserNotFound_thenThrownException() {
        when(userRepository.findById(wrongUserDto.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.create(itemDto, wrongUserDto.getId()));

        verify(itemMapper, never()).convertItemDto(any(ItemDto.class));
        verify(itemRepository, never()).save(any(Item.class));
        verify(itemMapper, never()).convertItem(any(Item.class));
        verify(userRepository, times(1)).findById(wrongUserDto.getId());
        assertThat(exception.getMessage(),
                is("Пользователя с id " + wrongUserDto.getId() + " не существует"));
    }

    @Test
    void update_whenSuccessful_thenReturnItemDto() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(updateItemDto.getId())).thenReturn(Optional.of(savedItem));
        when(itemMapper.convertItemDto(updateItemDto)).thenReturn(updateItem);
        when(itemRepository.save(updateItemToSave)).thenReturn(updateItemToSave);
        when(itemMapper.convertItem(updateItemToSave)).thenReturn(updateItemDto);

        ItemDto actualItemDto = itemService.update(updateItemDto, user.getId());

        assertThat(updateItemDto, is(actualItemDto));
        verify(userRepository, times(1)).findById(user.getId());
        verify(itemRepository, times(1)).findById(updateItemDto.getId());
        verify(itemMapper, times(1)).convertItemDto(updateItemDto);
        verify(itemRepository, times(1)).save(updateItemToSave);
        verify(itemMapper, times(1)).convertItem(updateItemToSave);
    }

    @Test
    void update_whenUserNotFound_thenThrownException() {
        when(userRepository.findById(wrongUserDto.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.update(updateItemDto, wrongUserDto.getId()));

        verify(userRepository, times(1)).findById(wrongUserDto.getId());
        verify(itemRepository, never()).findById(anyInt());
        verify(itemMapper, never()).convertItemDto(any(ItemDto.class));
        verify(itemRepository, never()).save(any(Item.class));
        verify(itemMapper, never()).convertItem(any(Item.class));
        assertThat(exception.getMessage(),
                is("Пользователя с id " + wrongUserDto.getId() + " не существует"));
    }

    @Test
    void update_whenItemNotFound_thenThrownException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(wrongUpdateItemDto.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.update(wrongUpdateItemDto, user.getId()));

        verify(userRepository, times(1)).findById(user.getId());
        verify(itemRepository, times(1)).findById(wrongUpdateItemDto.getId());
        verify(itemMapper, never()).convertItemDto(any(ItemDto.class));
        verify(itemRepository, never()).save(any(Item.class));
        verify(itemMapper, never()).convertItem(any(Item.class));
        assertThat(exception.getMessage(),
                is("Предмета с id " + wrongUpdateItemDto.getId() + " не существует"));
    }

    @Test
    void update_whenUserNotOwner_thenThrownException() {
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(updateItemDto.getId())).thenReturn(Optional.of(savedItem));
        when(itemMapper.convertItemDto(updateItemDto)).thenReturn(updateItem);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.update(updateItemDto, user2.getId()));

        verify(userRepository, times(1)).findById(user2.getId());
        verify(itemRepository, times(1)).findById(updateItemDto.getId());
        verify(itemMapper, times(1)).convertItemDto(updateItemDto);
        verify(itemRepository, never()).save(any(Item.class));
        verify(itemMapper, never()).convertItem(any(Item.class));
        assertThat(exception.getMessage(),
                is("Попытка изменить вещь пользователем, не являющимся владельцом"));
    }

    @Test
    void get_whenSuccessful_thenReturnItemDtoWithBookingsAndComments() {
        when(itemRepository.findById(savedItem.getId())).thenReturn(Optional.of(savedItem));
        when(itemMapper.convertItemToBookingDto(savedItem)).thenReturn(itemWithBookingsAndCommentsDtoConverted);
        when(bookingRepository.findAllByItemId(savedItem.getId())).thenReturn(List.of(booking, booking2));
        when(bookingMapper.convertBookingToShortDto(booking)).thenReturn(bookingShortDto);
        when(bookingMapper.convertBookingToShortDto(booking2)).thenReturn(bookingShortDto2);
        Sort sort = Sort.by("created").descending();
        when(commentRepository.findAllByItemId(savedItem.getId(), sort)).thenReturn(List.of(comment));
        when(commentMapper.convertComment(comment)).thenReturn(commentDto);

        ItemWithBookingsAndCommentsDto actualItemWithBookingsAndComments
                = itemService.get(savedItem.getId(), user.getId());

        assertThat(itemWithBookingsAndCommentsDto, is(actualItemWithBookingsAndComments));
        verify(itemRepository, times(1)).findById(savedItem.getId());
        verify(itemMapper, times(1)).convertItemToBookingDto(savedItem);
        verify(bookingRepository, times(1)).findAllByItemId(savedItem.getId());
        verify(bookingMapper, times(2)).convertBookingToShortDto(any(Booking.class));
        verify(commentRepository, times(1)).findAllByItemId(savedItem.getId(), sort);
        verify(commentMapper, times(1)).convertComment(comment);
    }

    @Test
    void search_whenSuccessful_thenReturnListOfItemDtos() {
        String text = "test";
        when(itemRepository.search(text, page)).thenReturn(List.of(savedItem));
        when(itemMapper.convertListItem(List.of(savedItem))).thenReturn(List.of(savedItemDto));

        List<ItemDto> actualItemDtos = itemService.search(text, page);

        assertThat(List.of(savedItemDto), is(actualItemDtos));
        verify(itemRepository, times(1)).search(text, page);
        verify(itemMapper, times(1)).convertListItem(List.of(savedItem));
    }

    @Test
    void search_whenEmptySearchText_thenReturnEmptyList() {
        String text = "";

        List<ItemDto> actualItemDtos = itemService.search(text, page);

        assertThat(List.of(), is(actualItemDtos));
        verify(itemRepository, never()).search(anyString(), any(PageRequest.class));
        verify(itemMapper, never()).convertListItem(anyList());
    }

    @Test
    void getAll_whenSuccessful_thenReturnListOfItemWithBookingsAndCommentsDto() {
        when(itemRepository.findAllByOwnerId(user.getId(), page)).thenReturn(List.of(savedItem));
        when(commentRepository.findAllByItemsId(List.of(savedItem.getId()))).thenReturn(List.of(comment));
        when(bookingRepository.findAllByItemsId(List.of(savedItem.getId()))).thenReturn(List.of(booking, booking2));
        when(itemMapper.convertItemToBookingDto(savedItem)).thenReturn(itemWithBookingsAndCommentsDtoConverted);
        when(bookingMapper.convertBookingToShortDto(booking)).thenReturn(bookingShortDto);
        when(bookingMapper.convertBookingToShortDto(booking2)).thenReturn(bookingShortDto2);
        when(commentMapper.convertComment(comment)).thenReturn(commentDto);

        List<ItemWithBookingsAndCommentsDto> actualItemWithBookingsAndCommentsDtos
                = itemService.getAll(user.getId(), page);

        assertThat(List.of(itemWithBookingsAndCommentsDto), is(actualItemWithBookingsAndCommentsDtos));
        verify(itemRepository, times(1)).findAllByOwnerId(user.getId(), page);
        verify(commentRepository, times(1)).findAllByItemsId(List.of(savedItem.getId()));
        verify(bookingRepository, times(1)).findAllByItemsId(List.of(savedItem.getId()));
        verify(itemMapper, times(1)).convertItemToBookingDto(savedItem);
        verify(bookingMapper, times(2)).convertBookingToShortDto(any(Booking.class));
        verify(commentMapper, times(1)).convertComment(comment);
    }
}
