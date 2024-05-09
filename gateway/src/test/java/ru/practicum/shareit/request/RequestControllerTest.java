package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.constant.Constants;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RequestController.class)
public class RequestControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private RequestClient requestClient;

    @Autowired
    private MockMvc mvc;


    private ItemRequestCreateDto noDescriptionRequestCreateDto;


    @BeforeEach
    void setup() {
        noDescriptionRequestCreateDto = new ItemRequestCreateDto();
    }

    @Test
    void create_whenNoRequestDescription_thenThrownException() throws Exception {
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(noDescriptionRequestCreateDto))
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,
                        result.getResolvedException()));
        verify(requestClient, never()).create(any(ItemRequestCreateDto.class), anyLong());

    }

    @Test
    void getAll_whenFromParamLessThan0_thenThrownException() throws Exception {
        mvc.perform(get("/requests/all?from=-1&size=2")
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(ConstraintViolationException.class,
                        result.getResolvedException()));
        verify(requestClient, never()).getAll(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getAll_whenSizeParamLessThan1_thenThrownException() throws Exception {
        mvc.perform(get("/requests/all?from=0&size=0")
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(result -> assertInstanceOf(ConstraintViolationException.class,
                        result.getResolvedException()));
        verify(requestClient, never()).getAll(anyLong(), anyInt(), anyInt());
    }
}
