package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private static int idCounter = 1;

    private final Map<Integer, User> users = new HashMap<>();
    private final Map<String, Integer> emailsById = new HashMap<>();

    @Override
    public User create(User user) {
        user.setId(idCounter);
        checkEmail(user.getEmail(), user.getId());
        users.put(user.getId(), user);
        emailsById.put(user.getEmail(), user.getId());
        increaseIdCounter();
        return user;
    }

    @Override
    public User update(User user) {
        checkEmail(user.getEmail(), user.getId());
        User existentUser = users.get(user.getId());

        users.put(user.getId(), user);
        emailsById.remove(existentUser.getEmail());
        emailsById.put(user.getEmail(), user.getId());
        return user;
    }

    @Override
    public void delete(int userId) {
        emailsById.remove(users.get(userId).getEmail());
        users.remove(userId);
    }

    @Override
    public List<User> getAll() {
        return List.copyOf(users.values());
    }

    @Override
    public User get(int userId) {
        return users.get(userId);
    }

    private static void increaseIdCounter() {
        idCounter++;
    }

    private void checkEmail(String email, int userId) {
        if (emailsById.get(email) == null) {
            return;
        }
        if (emailsById.get(email) != userId) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }
    }
}
