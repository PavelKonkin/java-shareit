package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingRequestHeaderException;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.constant.Constants;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ItemService itemService;
    @MockBean
    private CommentService commentService;

    private UserDto userDto;
    private UserDto userDto2;
    private ItemWithBookingsAndCommentsDto itemWithBookingsAndCommentsDto;
    private CommentDto commentDto;
    private CommentCreateDto commentCreateDto;
    private CommentCreateDto wrongCommentCreateDto;
    private BookingShortDto bookingShortDto;
    private ItemDto itemDto;
    private ItemDto wrongItemDto;
    private ItemDto updateItemDto;

    @BeforeEach
    void setup() {
        userDto = UserDto.builder()
                .id(1)
                .name("test user")
                .email("test@user.email")
                .build();
        userDto2 = UserDto.builder()
                .id(2)
                .name("test user2")
                .email("test@user2.email")
                .build();
        itemDto = ItemDto.builder()
                .id(1)
                .name("test item")
                .description("test description")
                .owner(userDto)
                .available(true)
                .requestId(1)
                .build();
        updateItemDto = ItemDto.builder()
                .id(1)
                .name("update test item")
                .description("update test description")
                .owner(userDto)
                .available(true)
                .requestId(1)
                .build();
        wrongItemDto = ItemDto.builder()
                .id(2)
                .description("test description")
                .owner(userDto)
                .available(true)
                .requestId(1)
                .build();
        commentDto = CommentDto.builder()
                .id(1)
                .author(userDto2)
                .text("comment text")
                .created(LocalDateTime.now())
                .item(itemDto)
                .authorName(userDto2.getName())
                .build();
        commentCreateDto = CommentCreateDto.builder()
                .text(commentDto.getText())
                .authorId(commentDto.getAuthor().getId())
                .itemId(commentDto.getItem().getId())
                .created(commentDto.getCreated())
                .build();
        wrongCommentCreateDto = CommentCreateDto.builder()
                .authorId(commentDto.getAuthor().getId())
                .itemId(commentDto.getItem().getId())
                .created(commentDto.getCreated())
                .build();
        bookingShortDto = BookingShortDto.builder()
                .id(1)
                .bookerId(2)
                .build();
        itemWithBookingsAndCommentsDto = ItemWithBookingsAndCommentsDto.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .owner(itemDto.getOwner())
                .available(itemDto.getAvailable())
                .comments(List.of(commentDto))
                .lastBooking(bookingShortDto)
                .build();
    }

    @Test
    @SneakyThrows
    void getAllOwn_whenSuccessful_thenReturnListOfItemsDtoWithBookingsAndComments() {
        when(itemService.getAll(userDto.getId(), 0, 10))
                .thenReturn(List.of(itemWithBookingsAndCommentsDto));

        mvc.perform(get("/items?from=0&size=10")
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemWithBookingsAndCommentsDto.getId()), Integer.class))
                .andExpect(jsonPath("$.[0].name", is(itemWithBookingsAndCommentsDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemWithBookingsAndCommentsDto.getDescription())))
                .andExpect(jsonPath("$.[0].owner.id", is(itemWithBookingsAndCommentsDto.getOwner().getId())))
                .andExpect(jsonPath("$.[0].comments.[0].id", is(commentDto.getId())))
                .andExpect(jsonPath("$.[0].lastBooking.id", is(bookingShortDto.getId())));
        verify(itemService, times(1)).getAll(userDto.getId(), 0, 10);
    }

    @Test
    @SneakyThrows
    void getAllOwn_whenNoUserHeader_thenThrownException() {
        mvc.perform(get("/items?from=0&size=10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,
                        result.getResolvedException()));
        verify(itemService, never()).getAll(anyInt(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getAllOwn_whenUserHeaderNegative_thenThrownException() {
        mvc.perform(get("/items?from=0&size=10")
                        .header(Constants.USER_HEADER, -1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(ConstraintViolationException.class,
                        result.getResolvedException()));
        verify(itemService, never()).getAll(anyInt(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void get_whenSuccessful_thenReturnItemWithBookingsAndCommentsDto() {
        when(itemService.get(itemDto.getId(), userDto.getId())).thenReturn(itemWithBookingsAndCommentsDto);

        mvc.perform(get("/items/" + itemDto.getId())
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithBookingsAndCommentsDto.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(itemWithBookingsAndCommentsDto.getName())))
                .andExpect(jsonPath("$.description", is(itemWithBookingsAndCommentsDto.getDescription())))
                .andExpect(jsonPath("$.owner.id", is(itemWithBookingsAndCommentsDto.getOwner().getId())))
                .andExpect(jsonPath("$.comments.[0].id", is(commentDto.getId())))
                .andExpect(jsonPath("$.lastBooking.id", is(bookingShortDto.getId())));
        verify(itemService, times(1)).get(itemDto.getId(), userDto.getId());
    }

    @Test
    @SneakyThrows
    void get_whenNoUserHeader_thenThrownException() {
        mvc.perform(get("/items/" + itemDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,
                        result.getResolvedException()));
        verify(itemService, never()).get(anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void get_whenUserHeaderNegative_thenThrownException() {
        mvc.perform(get("/items/" + itemDto.getId())
                        .header(Constants.USER_HEADER, -1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(ConstraintViolationException.class,
                        result.getResolvedException()));
        verify(itemService, never()).get(anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void search_whenSuccessful_thenReturnListOfItemDtos() {
        String text = "test";
        int from = 0;
        int size = 10;
        when(itemService.search(text, from, size)).thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search?text=" + text + "&from=" + from + "&size=" + size)
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemDto.getId()), Integer.class))
                .andExpect(jsonPath("$.[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.[0].owner.id", is(itemDto.getOwner().getId())));

        verify(itemService, times(1)).search(text, from, size);
    }

    @Test
    @SneakyThrows
    void search_whenFromNegative_thenThrownException() {
        String text = "test";
        int from = -1;
        int size = 10;

        mvc.perform(get("/items/search?text=" + text + "&from=" + from + "&size=" + size)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        verify(itemService, never()).search(text, from, size);
    }

    @Test
    @SneakyThrows
    void create_whenSuccessful_thenReturnItemDto() {
        when(itemService.create(itemDto, userDto.getId())).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.owner.id", is(itemDto.getOwner().getId())));

        verify(itemService, times(1)).create(itemDto, userDto.getId());
    }

    @Test
    @SneakyThrows
    void create_whenWrongRequestBody_thenThrownException() {
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(wrongItemDto))
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        verify(itemService, never()).create(wrongItemDto, userDto.getId());
    }

    @Test
    @SneakyThrows
    void update_whenSuccessful_thenReturnItemDto() {
        when(itemService.update(updateItemDto, userDto.getId())).thenReturn(updateItemDto);

        mvc.perform(patch("/items/" + itemDto.getId())
                        .content(mapper.writeValueAsString(updateItemDto))
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updateItemDto.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(updateItemDto.getName())))
                .andExpect(jsonPath("$.description", is(updateItemDto.getDescription())))
                .andExpect(jsonPath("$.owner.id", is(updateItemDto.getOwner().getId())));

        verify(itemService, times(1)).update(updateItemDto, userDto.getId());
    }

    @Test
    @SneakyThrows
    void update_whenItemIdNegative_thenThrownException() {
        mvc.perform(patch("/items/-1")
                        .content(mapper.writeValueAsString(updateItemDto))
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        verify(itemService, never()).update(updateItemDto, userDto.getId());
    }

    @Test
    @SneakyThrows
    void comment_whenSuccessful_thenReturnCommentDto() {
        when(commentService.create(commentCreateDto)).thenReturn(commentDto);

        mvc.perform(post("/items/" + itemDto.getId() + "/comment")
                        .content(mapper.writeValueAsString(commentCreateDto))
                        .header(Constants.USER_HEADER, userDto2.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Integer.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.author.id", is(commentDto.getAuthor().getId())))
                .andExpect(jsonPath("$.item.id", is(commentDto.getItem().getId())));

        verify(commentService, times(1)).create(commentCreateDto);
    }

    @Test
    @SneakyThrows
    void comment_whenWrongRequestBody_thenThrownException() {
        mvc.perform(post("/items/" + itemDto.getId() + "/comment")
                        .content(mapper.writeValueAsString(wrongCommentCreateDto))
                        .header(Constants.USER_HEADER, userDto2.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        verify(commentService, never()).create(wrongCommentCreateDto);
    }
}