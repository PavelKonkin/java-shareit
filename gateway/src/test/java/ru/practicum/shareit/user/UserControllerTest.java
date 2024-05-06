package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private UserClient userClient;
    @Autowired
    private MockMvc mvc;

    private UserDto wrongUserCreateDto;
    private UserUpdateDto wrongUserUpdateDto;

    @BeforeEach
    void setup() {
        wrongUserCreateDto = new UserDto(null, "user test");
        wrongUserUpdateDto = new UserUpdateDto("wrong_email_user_test.test", "update user test");
    }

    @Test
    void create_whenWrongEmail_thenThrownException() throws Exception {
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(wrongUserCreateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,
                        result.getResolvedException()));
        verify(userClient, never()).create(any(UserDto.class));
    }

    @Test
    void update_whenWrongEmail_thenThrownException() throws Exception {
        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(wrongUserUpdateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,
                        result.getResolvedException()));
        verify(userClient, never()).update(any(UserUpdateDto.class), anyLong());
    }
}
