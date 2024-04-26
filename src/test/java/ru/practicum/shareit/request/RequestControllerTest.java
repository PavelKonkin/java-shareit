package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import ru.practicum.shareit.constant.Constants;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    private ItemRequestCreateDto noDescriptionRequestCreateDto;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setup() {
        requestCreateDto = new ItemRequestCreateDto(
                "test description",
                1,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        noDescriptionRequestCreateDto = new ItemRequestCreateDto(
                null,
                1,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        itemRequestDto = new ItemRequestDto(
                1,
                "test description",
                requestCreateDto.getCreated(),
                null);
    }

    @Test
    @SneakyThrows
    void create_whenSuccessful_thenReturnItemRequestDto() {
        when(itemRequestService.create(requestCreateDto))
                .thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                .content(mapper.writeValueAsString(requestCreateDto))
                        .header(Constants.USER_HEADER, 1)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Integer.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", equalTo(itemRequestDto.getCreated().toString())));
        verify(itemRequestService, times(1)).create(requestCreateDto);
    }

    @Test
    @SneakyThrows
    void create_whenNoUserHeader_thenThrownException() {
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
    @SneakyThrows
    void create_whenNoRequestDescription_thenThrownException() {
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(noDescriptionRequestCreateDto))
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,
                        result.getResolvedException()));
        verify(itemRequestService, never()).create(Mockito.any(ItemRequestCreateDto.class));

    }

    @Test
    @SneakyThrows
    void getAllOwn_whenSuccessful_thenReturnItemRequestDtoList() {
        when(itemRequestService.getAllOwn(1))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests")
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemRequestDto.getId()), Integer.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.[0].created", equalTo(itemRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.[0].items", nullValue()));
        verify(itemRequestService, times(1)).getAllOwn(1);
    }

    @Test
    @SneakyThrows
    void getAllOwn_whenNoUserHeader_thenThrownException() {
        mvc.perform(get("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,
                        result.getResolvedException()));
        verify(itemRequestService, never()).getAllOwn(anyInt());
    }

    @Test
    @SneakyThrows
    void getAll_whenSuccessful_thenReturnItemRequestDtoList() {
        when(itemRequestService.getAll(2, 0, 2))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests/all?from=0&size=2")
                        .header(Constants.USER_HEADER, 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemRequestDto.getId()), Integer.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.[0].created", equalTo(itemRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.[0].items", nullValue()));
        verify(itemRequestService, times(1)).getAll(2, 0, 2);
    }

    @Test
    @SneakyThrows
    void getAll_whenRequestWithoutFromAndSize_thenReturnItemRequestDtoListWithDefaultParams() {
        when(itemRequestService.getAll(2, 0, 10))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests/all")
                        .header(Constants.USER_HEADER, 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(itemRequestDto.getId()), Integer.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.[0].created", equalTo(itemRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.[0].items", nullValue()));
        verify(itemRequestService, times(1)).getAll(2, 0, 10);
    }

    @Test
    @SneakyThrows
    void getAll_whenNoRecordsForUser_thenReturnEmptyItemRequestDtoList() {
        when(itemRequestService.getAll(1, 0, 2))
                .thenReturn(List.of());

        mvc.perform(get("/requests/all?from=0&size=2")
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", emptyIterable()));
        verify(itemRequestService, times(1)).getAll(1, 0, 2);
    }

    @Test
    @SneakyThrows
    void getAll_whenNoUserHeader_thenThrownException() {
        mvc.perform(get("/requests/all?from=0&size=2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,
                        result.getResolvedException()));
        verify(itemRequestService, never()).getAll(anyInt(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getAll_whenFromParamLessThan0_thenThrownException() {
        mvc.perform(get("/requests/all?from=-1&size=2")
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(ConstraintViolationException.class,
                        result.getResolvedException()));
        verify(itemRequestService, never()).getAll(anyInt(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getAll_whenSizeParamLessThan1_thenThrownException() {
        mvc.perform(get("/requests/all?from=0&size=0")
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(ConstraintViolationException.class,
                        result.getResolvedException()));
        verify(itemRequestService, never()).getAll(anyInt(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void get_whenSuccessful_thenReturnItemRequestDto() {
        when(itemRequestService.get(1, 1))
                .thenReturn(itemRequestDto);

        mvc.perform(get("/requests/1")
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Integer.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", equalTo(itemRequestDto.getCreated().toString())))
                .andExpect(jsonPath("$.items", nullValue()));
        verify(itemRequestService, times(1)).get(1,1);
    }

    @Test
    @SneakyThrows
    void get_whenNoUserHeader_thenThrownException() {
        mvc.perform(get("/requests/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,
                        result.getResolvedException()));
        verify(itemRequestService, never()).get(anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void get_whenRequestIdNegative_thenThrownException() {
        mvc.perform(get("/requests/-1")
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(ConstraintViolationException.class,
                        result.getResolvedException()));
        verify(itemRequestService, never()).get(anyInt(), anyInt());
    }
}
