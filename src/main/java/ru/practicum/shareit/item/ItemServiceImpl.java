package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final ItemMapper itemMapper;
    private final UserStorage userStorage;

    public ItemServiceImpl(ItemStorage itemStorage, ItemMapper itemMapper, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.itemMapper = itemMapper;
        this.userStorage = userStorage;
    }

    @Override
    public List<ItemDto> getItems(Integer userId) {
        List<Item> items = itemStorage.findByOwner(userId);
        return itemMapper.convertListItem(items);
    }

    @Override
    public ItemDto create(ItemDto itemDto, Integer userId) throws NotFoundException {
        User user = userStorage.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользоваетля с id " + userId + " не существует");
        }
        itemDto.setOwner(user);
        Item newItem = itemMapper.convertItemDto(itemDto);
        return itemMapper.convertItem(itemStorage.add(newItem));
    }

    @Override
    public ItemDto update(ItemDto itemDto, Integer userId) throws NotFoundException, ValidationException {
        User user = userStorage.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользоваетля с id " + userId + " не существует");
        }
        itemDto.setOwner(user);
        Item existentItem = itemStorage.findById(itemDto.getId());
        if (existentItem == null) {
            throw new NotFoundException("Предмета с id " + itemDto.getId() + " не существует");
        }
        Item newItem = itemMapper.convertItemDto(itemDto);
        if (!Objects.equals(existentItem.getOwner(), user)) {
            throw new NotFoundException("Попытка изменить вещь пользователем, не являющимся владельцом");
        }
        if (newItem.getName() == null) {
            newItem.setName(existentItem.getName());
        }
        if (newItem.getDescription() == null) {
            newItem.setDescription(existentItem.getDescription());
        }
        if (newItem.getAvailable() == null) {
            newItem.setAvailable(existentItem.getAvailable());
        }

        return itemMapper.convertItem(itemStorage.update(newItem));
    }

    @Override
    public ItemDto get(Integer itemId) throws NotFoundException {
        Item item = itemStorage.findById(itemId);
        if (item == null) {
            throw new NotFoundException("Предмета с id " + itemId + " не существует");
        }
        return itemMapper.convertItem(item);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> items = itemStorage.findByText(text);
        return itemMapper.convertListItem(items);
    }
}
