package ru.practicum.shareit.user;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAll();

    UserDto create(UserDto userDto) throws ValidationException;

    UserDto update(UserDto userDto) throws NotFoundException, ValidationException;

    void delete(Integer userId) throws NotFoundException;

    UserDto get(Integer userId) throws NotFoundException;
}
