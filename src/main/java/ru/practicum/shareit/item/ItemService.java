package ru.practicum.shareit.item;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getItems(Integer userId);

    ItemDto create(ItemDto itemDto, Integer userId) throws NotFoundException;

    ItemDto update(ItemDto itemDto, Integer userId) throws NotFoundException, ValidationException;

    ItemDto get(Integer itemId) throws NotFoundException;

    List<ItemDto> searchItems(String text);
}
