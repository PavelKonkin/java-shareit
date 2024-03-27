package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAll(int userId);

    ItemDto create(ItemDto itemDto, int userId);

    ItemDto update(ItemDto itemDto, int userId);

    ItemDto get(int itemId);

    List<ItemDto> search(String text);
}
