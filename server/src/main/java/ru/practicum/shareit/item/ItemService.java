package ru.practicum.shareit.item;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;

import java.util.List;

public interface ItemService {
    List<ItemWithBookingsAndCommentsDto> getAll(long userId, Pageable page);

    ItemDto create(ItemDto itemDto, long userId);

    ItemDto update(ItemDto itemDto, long userId);

    ItemWithBookingsAndCommentsDto get(long itemId, long userId);

    List<ItemDto> search(String text, Pageable page);
}
