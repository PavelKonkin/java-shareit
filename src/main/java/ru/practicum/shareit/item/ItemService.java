package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;

import java.util.List;

public interface ItemService {
    List<ItemWithBookingsAndCommentsDto> getAll(int userId, int from, int size);

    ItemDto create(ItemDto itemDto, int userId);

    ItemDto update(ItemDto itemDto, int userId);

    ItemWithBookingsAndCommentsDto get(int itemId, int userId);

    List<ItemDto> search(String text, int from, int size);
}
