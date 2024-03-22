package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public List<User> convertListUserDto(List<UserDto> list) {
        return list.stream()
                .map(this::convertUserDto)
                .collect(Collectors.toList());
    }

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
}
