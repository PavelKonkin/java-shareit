package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collections;
import java.util.List;

@Service
public class ItemService {
    private final ItemStorage itemStorage;
    private final ItemMapper itemMapper;
    private final UserStorage userStorage;

    public ItemService(ItemStorage itemStorage, ItemMapper itemMapper, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.itemMapper = itemMapper;
        this.userStorage = userStorage;
    }

    public List<ItemDto> getItems(Integer userId) {
        List<Item> items = itemStorage.findByOwner(userId);
        return itemMapper.convertListItem(items);
    }

    public ItemDto create(ItemDto itemDto, Integer userId) throws NotFoundException {
        userStorage.get(userId);
        Item newItem = itemMapper.convertItemDto(itemDto);
        newItem.setOwnerId(userId);
        return itemMapper.convertItem(itemStorage.add(newItem));
    }

    public ItemDto update(ItemDto itemDto, Integer itemId, Integer userId) throws NotFoundException {
        Item newItem = itemMapper.convertItemDto(itemDto);
        newItem.setId(itemId);
        newItem.setOwnerId(userId);
        return itemMapper.convertItem(itemStorage.update(newItem));
    }

    public ItemDto get(Integer itemId) throws NotFoundException {
        return itemMapper.convertItem(itemStorage.findById(itemId));
    }

    public List<ItemDto> searchItems(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> items = itemStorage.findByText(text);
        return itemMapper.convertListItem(items);
    }
}
