package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User create(User user) throws ValidationException {
        if (users.values().stream()
                .anyMatch(entity -> entity.getEmail().equals(user.getEmail()))
                ) {
            throw new ValidationException();
        }
        user.setId(User.getIdCounter());
        User.increaseIdCounter();
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) throws NotFoundException, ValidationException {
        if (users.containsKey(user.getId())) {
            if (users.values().stream()
                    .anyMatch(entity -> entity.getEmail().equals(user.getEmail())
                            && !Objects.equals(entity.getId(), user.getId()))
            ) {
                throw new ValidationException();
            }
            User existentUser = users.get(user.getId());
            if (user.getEmail() == null) {
                user.setEmail(existentUser.getEmail());
            }
            if (user.getName() == null) {
                user.setName(existentUser.getName());
            }
            users.put(user.getId(), user);
        } else {
            throw new NotFoundException();
        }
        return user;
    }

    @Override
    public void delete(Integer userId) throws NotFoundException {
        if (users.containsKey(userId)) {
            users.remove(userId);
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    public List<User> getAll() {
        return List.copyOf(users.values());
    }

    @Override
    public User get(Integer userId) throws NotFoundException {
        if (users.containsKey(userId)) {
            return users.get(userId);
        } else {
            throw new NotFoundException();
        }
    }
}
