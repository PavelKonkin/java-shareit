package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;

    private User user1;
    private User user2;
    private User wrongUser;
    private User wrongUpdateUser;
    private User updatedUser;
    private UserDto userDto1;
    private UserDto userDto2;
    private UserDto wrongUserDto;
    private UserUpdateDto userUpdateDto;
    private UserUpdateDto wrongUserUpdateDto;

    @BeforeEach
    void setup() {
        user1 = User.builder()
                .id(1L)
                .name("user name")
                .email("mail@user.test")
                .build();
        user2 = User.builder()
                .id(2L)
                .name("user name2")
                .email("mail@user2.test")
                .build();
        wrongUser = User.builder()
                .id(66L)
                .name("wrong user name")
                .email("wrong_mail@user.test")
                .build();
        userDto1 = UserDto.builder()
                .id(user1.getId())
                .name(user1.getName())
                .email(user1.getEmail())
                .build();
        userDto2 = UserDto.builder()
                .id(user2.getId())
                .name(user2.getName())
                .email(user2.getEmail())
                .build();
        userUpdateDto = UserUpdateDto.builder()
                .id(user1.getId())
                .name(user1.getName() + "update")
                .email("update" + user1.getEmail())
                .build();
        wrongUserUpdateDto = UserUpdateDto.builder()
                .id(66L)
                .name(user1.getName() + "update")
                .email("update" + user1.getEmail())
                .build();
        updatedUser = User.builder()
                .id(userUpdateDto.getId())
                .name(userUpdateDto.getName())
                .email(userUpdateDto.getEmail())
                .build();
        wrongUpdateUser = User.builder()
                .id(wrongUserUpdateDto.getId())
                .name(wrongUserUpdateDto.getName())
                .email(wrongUserUpdateDto.getEmail())
                .build();
        wrongUserDto = UserDto.builder()
                .id(wrongUser.getId())
                .name(wrongUser.getName())
                .email(wrongUser.getEmail())
                .build();
    }

    @Test
    void create_whenSuccessful_thenReturnUserDto() {
        when(userMapper.convertUserDto(userDto1)).thenReturn(user1);
        when(userRepository.save(user1)).thenReturn(user1);
        when(userMapper.convertUser(user1)).thenReturn(userDto1);

        UserDto actualUserDto = userService.create(userDto1);

        assertThat(userDto1, equalTo(actualUserDto));
        verify(userMapper, times(1)).convertUserDto(userDto1);
        verify(userMapper, times(1)).convertUser(user1);
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void delete_whenSuccessful_thenDoNothing() {
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        doNothing().when(userRepository).delete(user1);

        userService.delete(user1.getId());

        verify(userRepository, times(1)).delete(user1);
        verify(userRepository, times(1)).findById(user1.getId());
    }

    @Test
    void delete_whenUserNotFound_thenThrownException() {
        when(userRepository.findById(wrongUser.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.delete(wrongUser.getId()));

        verify(userRepository, never()).delete(wrongUser);
        verify(userRepository, times(1)).findById(wrongUser.getId());
        assertThat(exception.getMessage(), is("Пользователя с id " + wrongUser.getId() + " не существует"));
    }

    @Test
    void get_whenSuccessful_thenReturnUserDto() {
        when(userRepository.findById(userDto1.getId())).thenReturn(Optional.of(user1));
        when(userMapper.convertUser(user1)).thenReturn(userDto1);

        UserDto actualUserDto = userService.get(userDto1.getId());

        assertThat(actualUserDto, is(userDto1));
        verify(userRepository, times(1)).findById(userDto1.getId());
        verify(userMapper, times(1)).convertUser(user1);
    }

    @Test
    void get_whenUserNotFound_thenThrownException() {
        when(userRepository.findById(wrongUserDto.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.get(wrongUserDto.getId()));

        verify(userRepository, times(1)).findById(wrongUserDto.getId());
        verify(userMapper, never()).convertUser(wrongUser);
        assertThat(exception.getMessage(), is("Пользователя с id " + wrongUserDto.getId() + " не существует"));
    }

    @Test
    void getAll_whenThereAreUsers_thenReturnListOfUserDtos() {
        List<UserDto> expectedUserDtos = List.of(userDto1, userDto2);
        List<User> users = List.of(user1, user2);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.convertListUser(users)).thenReturn(expectedUserDtos);

        List<UserDto> actualUserDtos = userService.getAll();

        assertThat(actualUserDtos, is(expectedUserDtos));
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).convertListUser(users);
    }

    @Test
    void getAll_whenThereAreNoUsers_thenReturnEmptyList() {
        List<UserDto> expectedUserDtos = List.of();
        List<User> users = List.of();
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.convertListUser(users)).thenReturn(expectedUserDtos);

        List<UserDto> actualUserDtos = userService.getAll();

        assertThat(actualUserDtos, is(expectedUserDtos));
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).convertListUser(users);
    }

    @Test
    void update_whenSuccessful_thenReturnUserUpdateDtoWithUpdatedNameAndEmail() {
        when(userMapper.convertUserUpdateDto(userUpdateDto)).thenReturn(updatedUser);
        when(userRepository.findById(updatedUser.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(userMapper.convertUserToUserUpdateDto(updatedUser)).thenReturn(userUpdateDto);

        UserUpdateDto actualUserUpdateDto = userService.update(userUpdateDto);

        assertThat(userUpdateDto, is(actualUserUpdateDto));
        verify(userMapper, times(1)).convertUserUpdateDto(userUpdateDto);
        verify(userRepository, times(1)).findById(updatedUser.getId());
        verify(userRepository, times(1)).save(updatedUser);
        verify(userMapper, times(1)).convertUserToUserUpdateDto(updatedUser);
    }

    @Test
    void update_whenNameDoesntUpdate_thenReturnUserUpdateDtoWithUpdatedEmail() {
        userUpdateDto.setName(null);
        updatedUser.setName(null);
        UserUpdateDto returnedUserUpdateDto = userUpdateDto.toBuilder().name(user1.getName()).build();
        User userToUpdate = updatedUser.toBuilder().name(user1.getName()).build();
        when(userMapper.convertUserUpdateDto(userUpdateDto)).thenReturn(updatedUser);
        when(userRepository.findById(updatedUser.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(userToUpdate)).thenReturn(userToUpdate);
        when(userMapper.convertUserToUserUpdateDto(userToUpdate)).thenReturn(returnedUserUpdateDto);

        UserUpdateDto actualUserUpdateDto = userService.update(userUpdateDto);

        assertThat(returnedUserUpdateDto, is(actualUserUpdateDto));
        verify(userMapper, times(1)).convertUserUpdateDto(userUpdateDto);
        verify(userRepository, times(1)).findById(updatedUser.getId());
        verify(userRepository, times(1)).save(userToUpdate);
        verify(userMapper, times(1)).convertUserToUserUpdateDto(userToUpdate);
    }

    @Test
    void update_whenEmailDoesntUpdate_thenReturnUserUpdateDtoWithUpdatedName() {
        userUpdateDto.setEmail(null);
        updatedUser.setEmail(null);
        UserUpdateDto returnedUserUpdateDto = userUpdateDto.toBuilder().email(user1.getEmail()).build();
        User userToUpdate = updatedUser.toBuilder().email(user1.getEmail()).build();
        when(userMapper.convertUserUpdateDto(userUpdateDto)).thenReturn(updatedUser);
        when(userRepository.findById(updatedUser.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(userToUpdate)).thenReturn(userToUpdate);
        when(userMapper.convertUserToUserUpdateDto(userToUpdate)).thenReturn(returnedUserUpdateDto);

        UserUpdateDto actualUserUpdateDto = userService.update(userUpdateDto);

        assertThat(returnedUserUpdateDto, is(actualUserUpdateDto));
        verify(userMapper, times(1)).convertUserUpdateDto(userUpdateDto);
        verify(userRepository, times(1)).findById(updatedUser.getId());
        verify(userRepository, times(1)).save(userToUpdate);
        verify(userMapper, times(1)).convertUserToUserUpdateDto(userToUpdate);
    }

    @Test
    void update_whenEmailAndNameDoesntUpdate_thenReturnUserUpdateDtoWithoutUpdate() {
        userUpdateDto.setEmail(null);
        userUpdateDto.setName(null);
        updatedUser.setEmail(null);
        updatedUser.setName(null);
        UserUpdateDto returnedUserUpdateDto = userUpdateDto.toBuilder()
                .email(user1.getEmail())
                .name(user1.getName())
                .build();
        User userToUpdate = updatedUser.toBuilder()
                .email(user1.getEmail())
                .name(user1.getName())
                .build();
        when(userMapper.convertUserUpdateDto(userUpdateDto)).thenReturn(updatedUser);
        when(userRepository.findById(updatedUser.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(userToUpdate)).thenReturn(userToUpdate);
        when(userMapper.convertUserToUserUpdateDto(userToUpdate)).thenReturn(returnedUserUpdateDto);

        UserUpdateDto actualUserUpdateDto = userService.update(userUpdateDto);

        assertThat(returnedUserUpdateDto, is(actualUserUpdateDto));
        verify(userMapper, times(1)).convertUserUpdateDto(userUpdateDto);
        verify(userRepository, times(1)).findById(updatedUser.getId());
        verify(userRepository, times(1)).save(userToUpdate);
        verify(userMapper, times(1)).convertUserToUserUpdateDto(userToUpdate);
    }

    @Test
    void update_whenUserNotFound_thenThrowException() {
        when(userMapper.convertUserUpdateDto(wrongUserUpdateDto)).thenReturn(wrongUpdateUser);
        when(userRepository.findById(wrongUpdateUser.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.update(wrongUserUpdateDto));

        verify(userMapper, times(1)).convertUserUpdateDto(wrongUserUpdateDto);
        verify(userRepository, times(1)).findById(wrongUpdateUser.getId());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).convertUserToUserUpdateDto(any());
        assertThat(exception.getMessage(),
                is("Пользователя с id " + wrongUserUpdateDto.getId() + " не существует"));
    }
}
