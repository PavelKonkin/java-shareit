package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Integer, Item> items = new HashMap<>();

    @Override
    public Item add(Item item) {
        item.setId(Item.getIdCounter());
        Item.increaseIdCounter();
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) throws NotFoundException {
        if (items.containsKey(item.getId())) {
            Item existentItem = items.get(item.getId());
            if (!Objects.equals(existentItem.getOwnerId(), item.getOwnerId())) {
                throw new NotFoundException();
            }
            if (item.getName() == null) {
                item.setName(existentItem.getName());
            }
            if (item.getDescription() == null) {
                item.setDescription(existentItem.getDescription());
            }
            if (item.getAvailable() == null) {
                item.setAvailable(existentItem.getAvailable());
            }
            items.put(item.getId(), item);
        } else {
            throw new NotFoundException();
        }
        return item;
    }

    @Override
    public Item findById(Integer id) throws NotFoundException {
        if (items.containsKey(id)) {
            return items.get(id);
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    public List<Item> findAll(Integer userId) {
        return items.values().stream()
                .filter(item -> Objects.equals(item.getOwnerId(), userId))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Item> findByText(String text) {
        return items.values().stream()
                .filter(item -> (item.getName().toLowerCase().contains(text)
                        || item.getDescription().toLowerCase().contains(text)) && item.getAvailable())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Item> findByOwner(Integer userid) {
        return items.values().stream()
                .filter(item -> Objects.equals(item.getOwnerId(), userid))
                .collect(Collectors.toUnmodifiableList());
    }
}
