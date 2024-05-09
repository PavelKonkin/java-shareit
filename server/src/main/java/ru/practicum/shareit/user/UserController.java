package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getAll() {
        log.info("Получен запрос на список всех пользователей");
        List<UserDto> result = userService.getAll();
        log.info("Список пользователей сформирован: {}", result);
        return result;
    }

    @GetMapping("/{userId}")
    public UserDto get(@PathVariable long userId) {
        log.info("Получен запрос на получение пользователя с id {}", userId);
        UserDto result = userService.get(userId);
        log.info("Найден пользователь: {}", result);
        return result;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody UserDto userDto) {
        log.info("Получен запрос на создание пользователя  {}", userDto);
        UserDto result = userService.create(userDto);
        log.info("Создан пользователь: {}", result);
        return result;
    }

    @PatchMapping("/{userId}")
    public UserUpdateDto update(@RequestBody UserUpdateDto userUpdateDto,
                                @PathVariable long userId) {
        log.info("Получен запрос на обновление пользователя {} с id {}", userUpdateDto, userId);
        userUpdateDto.setId(userId);
        UserUpdateDto result = userService.update(userUpdateDto);
        log.info("Обновлен пользователь: {}", result);
        return result;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long userId) {
        log.info("Получен запрос на удаление пользователя с id {}", userId);
        userService.delete(userId);
        log.info("Удален пользователь с id  {}", userId);
    }
}
