package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userStorage, UserMapper userMapper) {
        this.userRepository = userStorage;
        this.userMapper = userMapper;
    }

    @Override
    public List<UserDto> getAll() {
        return userMapper.convertListUser(userRepository.findAll());
    }

    @Override
    public UserDto create(UserDto userDto) {
        return userMapper.convertUser(userRepository.save(userMapper.convertUserDto(userDto)));
    }

    @Override
    public UserUpdateDto update(UserUpdateDto userDto) {
        User newUser = userMapper.convertUserUpdateDto(userDto);
        User existentUser = userRepository.findById(newUser.getId())
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + newUser.getId() + " не существует"));
        User existentUserCopy = existentUser.toBuilder().build();


        if (newUser.getEmail() != null) {
            existentUserCopy.setEmail(newUser.getEmail());
        }
        if (newUser.getName() != null) {
            existentUserCopy.setName(newUser.getName());
        }

        return userMapper.convertUserToUserUpdateDto(userRepository.save(existentUserCopy));
    }

    @Override
    public void delete(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует"));
        userRepository.delete(user);
    }

    @Override
    public UserDto get(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует"));
        return userMapper.convertUser(user);
    }
}
