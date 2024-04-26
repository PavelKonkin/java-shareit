package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
public class UserIT {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

    private User user;
    private UserDto userDto;
    private UserDto userDtoToCreate;
    private UserUpdateDto userUpdateDto;

    @BeforeEach
    void setup() {
        user = User.builder()
                .name("user name")
                .email("user@email.test")
                .build();
        userRepository.save(user);
        userDto = userMapper.convertUser(user);
        userDtoToCreate = UserDto.builder()
                .name("user create name")
                .email("user_create@email.test")
                .build();
        userUpdateDto = UserUpdateDto.builder()
                .id(user.getId())
                .name("updated " + user.getName())
                .email("updated_" + user.getEmail())
                .build();
    }

    @Test
    void get_whenSuccessful_thenReturnUserDto() {
        UserDto actualUserDto = userService.get(user.getId());

        assertThat(userDto, is(actualUserDto));
    }

    @Test
    void delete_whenSuccessful_thenNoUserFoundInRepository() {
        userService.delete(user.getId());

        Optional<User> actualUser = userRepository.findById(user.getId());

        assertTrue(actualUser.isEmpty());
    }

    @Test
    void create_whenSuccessful_thenReturnUserDto() {
        UserDto actualUserDto = userService.create(userDtoToCreate);

        UserDto savedUserDto = userService.get(actualUserDto.getId());

        assertThat(savedUserDto, is(actualUserDto));
    }

    @Test
    void getAll_whenSuccessful_thenReturnListOfUserDtos() {
        List<UserDto> expectedUserDtos = List.of(userDto);

        List<UserDto> actualUserDtos = userService.getAll();

        assertThat(expectedUserDtos, is(actualUserDtos));
    }

    @Test
    void update_whenSuccessful_thenReturnUpdatedUserDto() {
        UserUpdateDto expectedUserUpdateDto = UserUpdateDto.builder()
                .id(user.getId())
                .name(userUpdateDto.getName())
                .email(userUpdateDto.getEmail())
                .build();

        UserUpdateDto actualuserUpdateDto = userService.update(userUpdateDto);

        assertThat(expectedUserUpdateDto, is(actualuserUpdateDto));
    }
}
