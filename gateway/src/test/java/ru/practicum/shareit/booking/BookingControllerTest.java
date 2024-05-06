package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.constant.Constants;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private BookingClient bookingClient;

    private BookItemRequestDto wrongBookingCreateDto;

    @BeforeEach
    void setup() {
        wrongBookingCreateDto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS),
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS));

    }

    @Test
    void getAll_whenWrongStateRequestParam_thenThrownException() throws Exception {
        mvc.perform(get("/bookings?state=wrong&from=0&size=10")
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertInstanceOf(IllegalArgumentException.class,
                        result.getResolvedException()));
        verify(bookingClient, never())
                .getBookings(anyLong(), any(BookingState.class), anyInt(), anyInt());
    }

    @Test
    void getAllByOwner_whenFromRequestParamNegative_thenThrownException() throws Exception {
        mvc.perform(get("/bookings/owner?state=WAITING&from=-1&size=10")
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(ConstraintViolationException.class,
                        result.getResolvedException()));
        verify(bookingClient, never())
                .getAllForOwner(anyLong(), any(BookingState.class), anyInt(), anyInt());
    }

    @Test
    void create_whenStartLaterThanEnd_thenThrownException() throws Exception {
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(wrongBookingCreateDto))
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,
                        result.getResolvedException()));

        verify(bookingClient, never()).bookItem(anyLong(), any(BookItemRequestDto.class));
    }
}
