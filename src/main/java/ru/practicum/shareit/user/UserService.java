package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAll();

    UserDto create(UserDto userDto);

    UserUpdateDto update(UserUpdateDto userUpdateDto);

    void delete(int userId);

    UserDto get(int userId);
}
