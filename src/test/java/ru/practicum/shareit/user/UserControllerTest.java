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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private UserService userService;
    @Autowired
    private MockMvc mvc;
    private UserDto userDto;
    private UserDto wrongUserDto;
    private UserDto userCreateDto;
    private UserDto wrongUserCreateDto;
    private UserUpdateDto userUpdateDto;
    private UserUpdateDto wrongUserUpdateDto;

    @BeforeEach
    void setup() {
        userDto = UserDto.builder()
                .id(1)
                .name("user test")
                .email("user@test.test")
                .build();
        userCreateDto = UserDto.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
        wrongUserCreateDto = UserDto.builder()
                .name(userDto.getName())
                .build();
        wrongUserDto = UserDto.builder()
                .id(66)
                .name("wrong user test")
                .email("wrong_user@test.test")
                .build();
        userUpdateDto = UserUpdateDto.builder()
                .id(1)
                .name("update user test")
                .email("user_update@test.test")
                .build();
        wrongUserUpdateDto = UserUpdateDto.builder()
                .id(1)
                .name("update user test")
                .email("wrong_email_user_test.test")
                .build();
    }

    @Test
    void get_whenSuccessful_thenReturnUserDto() throws Exception {
        when(userService.get(userDto.getId())).thenReturn(userDto);

        mvc.perform(get("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", equalTo(userDto.getEmail())));
        verify(userService, times(1)).get(userDto.getId());
    }

    @Test
    void get_whenUserNotFound_thenThrownException() throws Exception {
        when(userService.get(wrongUserDto.getId()))
                .thenThrow(new NotFoundException("Пользователя с id " + wrongUserDto.getId() + " не существует"));

        mvc.perform(get("/users/" + wrongUserDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(NotFoundException.class,
                        result.getResolvedException()));
        verify(userService, times(1)).get(wrongUserDto.getId());
    }

    @Test
    void create_whenSuccessful_thenReturnUserDto() throws Exception {
        when(userService.create(userCreateDto)).thenReturn(userDto);

        mvc.perform(post("/users")
                .content(mapper.writeValueAsString(userCreateDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", equalTo(userDto.getEmail())));
        verify(userService, times(1)).create(userCreateDto);
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
        verify(userService, never()).create(any(UserDto.class));
    }

    @Test
    void getAll_whenSuccessful_thenReturnListOfUserDtos() throws Exception {
        when(userService.getAll()).thenReturn(List.of(userDto));

        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(userDto.getId()), Integer.class))
                .andExpect(jsonPath("$.[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$.[0].email", is(userDto.getEmail())));
        verify(userService, times(1)).getAll();
    }

    @Test
    void update_whenSuccessful_thenReturnUserDto() throws Exception {
        when(userService.update(userUpdateDto)).thenReturn(userUpdateDto);

        mvc.perform(patch("/users/1")
                .content(mapper.writeValueAsString(userUpdateDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userUpdateDto.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(userUpdateDto.getName())))
                .andExpect(jsonPath("$.email", equalTo(userUpdateDto.getEmail())));
        verify(userService, times(1)).update(userUpdateDto);
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
        verify(userService, never()).update(wrongUserUpdateDto);
    }

    @Test
    void delete_whenSuccessful_thenOkStatus() throws Exception {
        doNothing().when(userService).delete(1);
        mvc.perform(delete("/users/1")
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(userService, times(1)).delete(1);
    }

    @Test
    void delete_whenUserNotFound_thenThrownException() throws Exception {
        doThrow(new NotFoundException("")).when(userService).delete(2);
        mvc.perform(delete("/users/2")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(NotFoundException.class,
                        result.getResolvedException()));
        verify(userService, times(1)).delete(2);
    }
}
