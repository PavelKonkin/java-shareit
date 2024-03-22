package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item add(Item item);

    Item update(Item item) throws NotFoundException;

    Item findById(Integer id) throws NotFoundException;

    List<Item> findAll(Integer userId);

    List<Item> findByText(String text);

    List<Item> findByOwner(Integer userid);
}
