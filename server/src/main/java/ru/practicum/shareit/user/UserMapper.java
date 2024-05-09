package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public List<UserDto> convertListUser(List<User> list) {
        return list.stream()
                .map(this::convertUser)
                .collect(Collectors.toList());
    }

    public User convertUserDto(UserDto userDto) {
        return User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .id(userDto.getId())
                .build();
    }

    public UserDto convertUser(User user) {
        return UserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .id(user.getId())
                .build();
    }

    public User convertUserUpdateDto(UserUpdateDto userUpdateDto) {
        return User.builder()
                .name(userUpdateDto.getName())
                .email(userUpdateDto.getEmail())
                .id(userUpdateDto.getId())
                .build();
    }

    public UserUpdateDto convertUserToUserUpdateDto(User user) {
        return UserUpdateDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .id(user.getId())
                .build();
    }
}
