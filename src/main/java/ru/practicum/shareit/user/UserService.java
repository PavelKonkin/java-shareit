package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserStorage userStorage, UserMapper userMapper) {
        this.userStorage = userStorage;
        this.userMapper = userMapper;
    }

    public List<UserDto> getAll() {
        return userMapper.convertListUser(userStorage.getAll());
    }

    public UserDto create(UserDto userDto) throws ValidationException {
        return userMapper.convertUser(userStorage.create(userMapper.convertUserDto(userDto)));
    }

    public UserDto update(UserDto userDto, Integer userId) throws NotFoundException, ValidationException {
        User user = userMapper.convertUserDto(userDto);
        user.setId(userId);
        return userMapper.convertUser(userStorage.update(user));
    }

    public void delete(Integer userId) throws NotFoundException {
        userStorage.delete(userId);
    }

    public UserDto get(Integer userId) throws NotFoundException {
        return userMapper.convertUser(userStorage.get(userId));
    }
}
