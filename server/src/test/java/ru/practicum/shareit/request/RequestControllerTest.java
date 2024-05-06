package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingRequestHeaderException;
import ru.practicum.shareit.constant.Constants;
import ru.practicum.shareit.page.OffsetPage;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class RequestControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    private ItemRequestCreateDto requestCreateDto;
    private ItemRequestDto itemRequestDto;
    private final Sort sort = Sort.by("created");
    private final Pageable page = new OffsetPage(0, 10, sort);

    @BeforeEach
    void setup() {
        requestCreateDto = new ItemRequestCreateDto(
                "test description",
                1L,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        itemRequestDto = new ItemRequestDto(
                1L,
                "test description",
                requestCreateDto.getCreated(),
                null);
    }

    @Test
    void create_whenSuccessful_thenReturnItemRequestDto() throws Exception {
        when(itemRequestService.create(requestCreateDto))
                .thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                .content(mapper.writeValueAsString(requestCreateDto))
                        .header(Constants.USER_HEADER, 1)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", equalTo(itemRequestDto.getCreated().toString())));
        verify(itemRequestService, times(1)).create(requestCreateDto);
    }

    @Test
    void create_whenNoUserHeader_thenThrownException() throws Exception {
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestCreateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,
                        result.getResolvedException()));
        verify(itemRequestService, never()).create(Mockito.any(ItemRequestCreateDto.class));
    }

    @Test
    void getAllOwn_whenSuccessful_thenReturnItemRequestDtoList() throws Exception {
        when(itemRequestService.getAllOwn(1, sort))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests")
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.[0].created", equalTo(itemRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.[0].items", nullValue()));
        verify(itemRequestService, times(1)).getAllOwn(1, sort);
    }

    @Test
    void getAllOwn_whenNoUserHeader_thenThrownException() throws Exception {
        mvc.perform(get("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,
                        result.getResolvedException()));
        verify(itemRequestService, never()).getAllOwn(anyInt(), any());
    }

    @Test
    void getAll_whenSuccessful_thenReturnItemRequestDtoList() throws Exception {
        when(itemRequestService.getAll(2, page))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests/all?from=0&size=10")
                        .header(Constants.USER_HEADER, 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.[0].created", equalTo(itemRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.[0].items", nullValue()));
        verify(itemRequestService, times(1)).getAll(2, page);
    }

    @Test
    void getAll_whenNoRecordsForUser_thenReturnEmptyItemRequestDtoList() throws Exception {
        when(itemRequestService.getAll(1, page))
                .thenReturn(List.of());

        mvc.perform(get("/requests/all?from=0&size=10")
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", emptyIterable()));
        verify(itemRequestService, times(1)).getAll(1, page);
    }

    @Test
    void getAll_whenNoUserHeader_thenThrownException() throws Exception {
        mvc.perform(get("/requests/all?from=0&size=2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,
                        result.getResolvedException()));
        verify(itemRequestService, never()).getAll(anyInt(), any());
    }

    @Test
    void get_whenSuccessful_thenReturnItemRequestDto() throws Exception {
        when(itemRequestService.get(1, 1))
                .thenReturn(itemRequestDto);

        mvc.perform(get("/requests/1")
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", equalTo(itemRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.items", nullValue()));
        verify(itemRequestService, times(1)).get(1,1);
    }

    @Test
    void get_whenNoUserHeader_thenThrownException() throws Exception {
        mvc.perform(get("/requests/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,
                        result.getResolvedException()));
        verify(itemRequestService, never()).get(anyInt(), anyInt());
    }
}
