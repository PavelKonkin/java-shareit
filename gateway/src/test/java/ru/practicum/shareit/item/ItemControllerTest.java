package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.constant.Constants;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ItemClient itemClient;

    private CommentCreateDto wrongCommentCreateDto;
    private ItemDto wrongItemDto;

    @BeforeEach
    void setup() {
        wrongCommentCreateDto = new CommentCreateDto();
        wrongItemDto = new ItemDto();
    }

    @Test
    void search_whenFromNegative_thenThrownException() throws Exception {
        String text = "test";
        int from = -1;
        int size = 10;

        mvc.perform(get("/items/search?text=" + text + "&from=" + from + "&size=" + size)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        verify(itemClient, never()).search(anyString(), anyInt(), anyInt());
    }

    @Test
    void create_whenWrongRequestBody_thenThrownException() throws Exception {
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(wrongItemDto))
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        verify(itemClient, never()).create(wrongItemDto, 1L);
    }

    @Test
    void comment_whenWrongRequestBody_thenThrownException() throws Exception {
        mvc.perform(post("/items/" + 1 + "/comment")
                        .content(mapper.writeValueAsString(wrongCommentCreateDto))
                        .header(Constants.USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        verify(itemClient, never()).comment(any(CommentCreateDto.class), anyLong(), anyLong());
    }
}
