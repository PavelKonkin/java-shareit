package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.constant.Constants;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private BookingService bookingService;

    private BookingCreateDto bookingCreateDto;
    private BookingCreateDto wrongBookingCreateDto;
    private BookingDto bookingDto;
    private UserDto userDto;
    private UserDto userDto2;
    private ItemDto itemDto;


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
        bookingCreateDto = BookingCreateDto.builder()
                .bookerId(userDto2.getId())
                .itemId(itemDto.getId())
                .start(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS))
                .end(LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS))
                .status(BookingStateDto.WAITING)
                .build();
        wrongBookingCreateDto = BookingCreateDto.builder()
                .bookerId(userDto2.getId())
                .itemId(itemDto.getId())
                .start(LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS))
                .end(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS))
                .status(BookingStateDto.WAITING)
                .build();
        bookingDto = BookingDto.builder()
                .id(1)
                .booker(userDto2)
                .item(itemDto)
                .status(BookingStateDto.WAITING)
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .build();
    }

    @Test
    @SneakyThrows
    void create_whenSuccessful_thenReturnBookingDto() {
        when(bookingService.create(bookingCreateDto)).thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingCreateDto))
                        .header(Constants.USER_HEADER, userDto2.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Integer.class))
                .andExpect(jsonPath("$.booker.id", is(userDto2.getId())))
                .andExpect(jsonPath("$.item.id", is(itemDto.getId())))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.status").value(BookingStateDto.WAITING.toString()));

        verify(bookingService, times(1)).create(bookingCreateDto);
    }

    @Test
    @SneakyThrows
    void create_whenStartLaterThanEnd_thenThrownException() {
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(wrongBookingCreateDto))
                        .header(Constants.USER_HEADER, userDto2.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,
                        result.getResolvedException()));

        verify(bookingService, never()).create(any(BookingCreateDto.class));
    }

    @Test
    @SneakyThrows
    void confirmReject_whenSuccessful_thenReturnConfirmedBookingDto() {
        bookingDto.setStatus(BookingStateDto.APPROVED);
        when(bookingService.confirmReject(userDto.getId(), bookingDto.getId(), "true")).thenReturn(bookingDto);

        mvc.perform(patch("/bookings/" + bookingDto.getId() + "?approved=true")
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Integer.class))
                .andExpect(jsonPath("$.booker.id", is(userDto2.getId())))
                .andExpect(jsonPath("$.item.id", is(itemDto.getId())))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.status").value(BookingStateDto.APPROVED.toString()));
        verify(bookingService, times(1))
                .confirmReject(userDto.getId(), bookingDto.getId(), "true");
    }

    @Test
    @SneakyThrows
    void confirmReject_whenNoApprovedRequestParam_thenThrownException() {
        mvc.perform(patch("/bookings/" + bookingDto.getId())
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(MissingServletRequestParameterException.class,
                        result.getResolvedException()));
        verify(bookingService, never())
                .confirmReject(anyInt(), anyInt(), anyString());
    }

    @Test
    @SneakyThrows
    void get_whenSuccessful_thenReturnBookingDto() {
        when(bookingService.get(userDto.getId(), bookingDto.getId())).thenReturn(bookingDto);

        mvc.perform(get("/bookings/" + bookingDto.getId())
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Integer.class))
                .andExpect(jsonPath("$.booker.id", is(userDto2.getId())))
                .andExpect(jsonPath("$.item.id", is(itemDto.getId())))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.status").value(BookingStateDto.WAITING.toString()));
        verify(bookingService, times(1)).get(userDto.getId(), bookingDto.getId());
    }

    @Test
    @SneakyThrows
    void get_whenNegativeBookingIdPathVar_thenReturnBookingDto() {
        mvc.perform(get("/bookings/-1")
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(ConstraintViolationException.class,
                        result.getResolvedException()));
        verify(bookingService, never()).get(anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getAll_whenSuccessful_thenReturnListOfBookingDtos() {
        when(bookingService
                .getAllForBooker(userDto2.getId(), BookingStateDto.WAITING, 0, 10))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings?state=WAITING&from=0&size=10")
                        .header(Constants.USER_HEADER, userDto2.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Integer.class))
                .andExpect(jsonPath("$.[0].booker.id", is(userDto2.getId())))
                .andExpect(jsonPath("$.[0].item.id", is(itemDto.getId())))
                .andExpect(jsonPath("$.[0].start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.[0].end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.[0].status").value(BookingStateDto.WAITING.toString()));
        verify(bookingService, times(1))
                .getAllForBooker(userDto2.getId(), BookingStateDto.WAITING, 0, 10);
    }

    @Test
    @SneakyThrows
    void getAll_whenWrongStateRequestParam_thenThrownException() {
        mvc.perform(get("/bookings?state=waiting&from=0&size=10")
                        .header(Constants.USER_HEADER, userDto2.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertInstanceOf(IllegalArgumentException.class,
                        result.getResolvedException()));
        verify(bookingService, never())
                .getAllForBooker(anyInt(), any(BookingStateDto.class), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getAllByOwner_whenSuccessful_thenReturnListOfBookingDtos() {
        when(bookingService
                .getAllForOwner(userDto.getId(), BookingStateDto.WAITING, 0, 10))
                .thenReturn(List.of(bookingDto));

        mvc.perform(get("/bookings/owner?state=WAITING&from=0&size=10")
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(bookingDto.getId()), Integer.class))
                .andExpect(jsonPath("$.[0].booker.id", is(userDto2.getId())))
                .andExpect(jsonPath("$.[0].item.id", is(itemDto.getId())))
                .andExpect(jsonPath("$.[0].start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.[0].end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.[0].status").value(BookingStateDto.WAITING.toString()));
        verify(bookingService, times(1))
                .getAllForOwner(userDto.getId(), BookingStateDto.WAITING, 0, 10);
    }

    @Test
    @SneakyThrows
    void getAllByOwner_whenFromRequestParamNegative_thenThrownException() {
        mvc.perform(get("/bookings/owner?state=WAITING&from=-1&size=10")
                        .header(Constants.USER_HEADER, userDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(ConstraintViolationException.class,
                        result.getResolvedException()));
        verify(bookingService, never())
                .getAllForOwner(anyInt(), any(BookingStateDto.class), anyInt(), anyInt());
    }
}
