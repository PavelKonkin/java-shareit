package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingRequestHeaderException;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.constant.Constants;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.page.OffsetPage;
import ru.practicum.shareit.user.dto.UserDto;

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
    private BookingShortDto bookingShortDto;
    private ItemDto itemDto;
    private ItemDto updateItemDto;

    @BeforeEach
    void setup() {
        userDto = UserDto.builder()
                .id(1L)
                .name("test user")
                .email("test@user.email")
                .build();
        userDto2 = UserDto.builder()
                .id(2L)
                .name("test user2")
                .email("test@user2.email")
                .build();
        itemDto = ItemDto.builder()
                .id(1L)
                .name("test item")
                .description("test description")
                .available(true)
                .requestId(1L)
                .build();
        updateItemDto = ItemDto.builder()
                .id(1L)
                .name("update test item")
                .description("update test description")
                .available(true)
                .requestId(1L)
                .build();
        commentDto = CommentDto.builder()
                .id(1L)
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
        bookingShortDto = BookingShortDto.builder()
                .id(1L)
                .bookerId(2L)
                .build();
        itemWithBookingsAndCommentsDto = ItemWithBookingsAndCommentsDto.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .comments(List.of(commentDto))
                .lastBooking(bookingShortDto)
                .build();
    }

    @Test
    void getAllOwn_whenSuccessful_thenReturnListOfItemsDtoWithBookingsAndComments() throws Exception {
        long userId = 1;
        Sort sort = Sort.by("id");
        Pageable page = new OffsetPage(0, 10, sort);
        when(itemService.getAll(userId, page))
                .thenReturn(List.of(itemWithBookingsAndCommentsDto));

        mvc.perform(get("/items?from=0&size=10")
                        .header(Constants.USER_HEADER, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemWithBookingsAndCommentsDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(itemWithBookingsAndCommentsDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemWithBookingsAndCommentsDto.getDescription())))
                .andExpect(jsonPath("$.[0].comments.[0].id", is(commentDto.getId().intValue())))
                .andExpect(jsonPath("$.[0].lastBooking.id", is((int) bookingShortDto.getId())));
        verify(itemService, times(1)).getAll(userId, page);
    }

    @Test
    void getAllOwn_whenNoUserHeader_thenThrownException() throws Exception {
        mvc.perform(get("/items?from=0&size=10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,
                        result.getResolvedException()));
        verify(itemService, never()).getAll(anyInt(), any(Pageable.class));
    }

    @Test
    void get_whenSuccessful_thenReturnItemWithBookingsAndCommentsDto() throws Exception {
        when(itemService.get(itemDto.getId(), userDto.getId())).thenReturn(itemWithBookingsAndCommentsDto);

        mvc.perform(get("/items/" + itemDto.getId())
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithBookingsAndCommentsDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemWithBookingsAndCommentsDto.getName())))
                .andExpect(jsonPath("$.description", is(itemWithBookingsAndCommentsDto.getDescription())))
                .andExpect(jsonPath("$.comments.[0].id", is(commentDto.getId().intValue())))
                .andExpect(jsonPath("$.lastBooking.id", is((int) bookingShortDto.getId())));
        verify(itemService, times(1)).get(itemDto.getId(), userDto.getId());
    }

    @Test
    void get_whenNoUserHeader_thenThrownException() throws Exception {
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
    void search_whenSuccessful_thenReturnListOfItemDtos() throws Exception {
        String text = "test";
        int from = 0;
        int size = 10;
        Sort sort = Sort.by("id");
        Pageable page = new OffsetPage(from, size, sort);
        when(itemService.search(text, page)).thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search?text=" + text + "&from=" + from + "&size=" + size)
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemDto.getDescription())));

        verify(itemService, times(1)).search(text, page);
    }

    @Test
    void create_whenSuccessful_thenReturnItemDto() throws Exception {
        when(itemService.create(itemDto, userDto.getId())).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));

        verify(itemService, times(1)).create(itemDto, userDto.getId());
    }

    @Test
    void update_whenSuccessful_thenReturnItemDto() throws Exception {
        when(itemService.update(updateItemDto, userDto.getId())).thenReturn(updateItemDto);

        mvc.perform(patch("/items/" + itemDto.getId())
                        .content(mapper.writeValueAsString(updateItemDto))
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updateItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updateItemDto.getName())))
                .andExpect(jsonPath("$.description", is(updateItemDto.getDescription())));

        verify(itemService, times(1)).update(updateItemDto, userDto.getId());
    }

    @Test
    void comment_whenSuccessful_thenReturnCommentDto() throws Exception {
        when(commentService.create(commentCreateDto)).thenReturn(commentDto);

        mvc.perform(post("/items/" + itemDto.getId() + "/comment")
                        .content(mapper.writeValueAsString(commentCreateDto))
                        .header(Constants.USER_HEADER, userDto2.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.author.id", is(commentDto.getAuthor().getId().intValue())))
                .andExpect(jsonPath("$.item.id", is(commentDto.getItem().getId().intValue())));

        verify(commentService, times(1)).create(commentCreateDto);
    }
}
