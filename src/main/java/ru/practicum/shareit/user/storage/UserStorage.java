package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;

import java.util.List;

public interface UserStorage {
    User create(User user) throws ValidationException;

    User update(User user) throws NotFoundException, ValidationException;

    void delete(Integer userId) throws NotFoundException;

    List<User> getAll();

    User get(Integer userId) throws NotFoundException;
}
