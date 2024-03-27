package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$";

    @Autowired
    public UserServiceImpl(UserStorage userStorage, UserMapper userMapper) {
        this.userStorage = userStorage;
        this.userMapper = userMapper;
    }

    @Override
    public List<UserDto> getAll() {
        return userMapper.convertListUser(userStorage.getAll());
    }

    @Override
    public UserDto create(UserDto userDto) {
        return userMapper.convertUser(userStorage.create(userMapper.convertUserDto(userDto)));
    }

    @Override
    public UserDto update(UserDto userDto) {
        User newUser = userMapper.convertUserDto(userDto);
        User existentUser = userStorage.get(newUser.getId());
        if (existentUser == null) {
            throw new NotFoundException("Пользователя с id " + newUser.getId() + " не существует");
        }
        User existentUserCopy = existentUser.toBuilder().build();


        if (newUser.getEmail() != null) {
            checkEmail(newUser.getEmail());
            existentUserCopy.setEmail(newUser.getEmail());
        }
        if (newUser.getName() != null) {
            existentUserCopy.setName(newUser.getName());
        }

        return userMapper.convertUser(userStorage.update(existentUserCopy));
    }

    @Override
    public void delete(int userId) {
        User user = userStorage.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователя с id " + userId + " не существует");
        }
        userStorage.delete(userId);
    }

    @Override
    public UserDto get(int userId) {
        User user = userStorage.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователя с id " + userId + " не существует");
        }
        return userMapper.convertUser(user);
    }

    private void checkEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            throw new ValidationException("Адрес не является адресом электронной почты");
        }
    }
}
