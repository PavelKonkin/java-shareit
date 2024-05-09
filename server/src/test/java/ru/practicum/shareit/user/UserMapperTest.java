package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {
    @InjectMocks
    private UserMapper userMapper;

    private User user;
    private UserDto userDto;
    private UserUpdateDto userUpdateDto;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .name("test user")
                .email("test@user.email")
                .build();
        userDto = UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
        userUpdateDto = UserUpdateDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @Test
    void convertUser_whenSuccessful_thenReturnUserDto() {
        UserDto actualUserDto = userMapper.convertUser(user);

        assertThat(userDto, is(actualUserDto));
    }

    @Test
    void convertUserDto_whenSuccessful_thenReturnUser() {
        User actualUser = userMapper.convertUserDto(userDto);

        assertThat(user, is(actualUser));
    }

    @Test
    void convertUserUpdateDto_whenSuccessful_thenReturnUser() {
        User actualUser = userMapper.convertUserUpdateDto(userUpdateDto);

        assertThat(user, is(actualUser));
    }

    @Test
    void convertUserToUserUpdateDto_whenSuccessful_thenReturnUserUpdateDto() {
        UserUpdateDto actualUserUpdateDto = userMapper.convertUserToUserUpdateDto(user);

        assertThat(userUpdateDto, is(actualUserUpdateDto));
    }

    @Test
    void convertListUser_whenSuccessful_thenReturnListOfUserDtos() {
        List<UserDto> actualListOfUserDtos = userMapper.convertListUser(List.of(user));

        assertThat(List.of(userDto), is(actualListOfUserDtos));
    }
}
