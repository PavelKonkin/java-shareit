package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item add(Item item);

    Item update(Item item);

    Item findById(int id);

    List<Item> findByText(String text);

    List<Item> findByOwner(int userid);
}
