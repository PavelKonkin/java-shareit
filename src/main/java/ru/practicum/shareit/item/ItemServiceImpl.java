package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final UserStorage userStorage;

    public ItemServiceImpl(ItemStorage itemStorage, ItemMapper itemMapper,
                           UserStorage userStorage, UserMapper userMapper) {
        this.itemStorage = itemStorage;
        this.itemMapper = itemMapper;
        this.userStorage = userStorage;
        this.userMapper = userMapper;
    }

    @Override
    public List<ItemDto> getAll(int userId) {
        List<Item> items = itemStorage.findByOwner(userId);
        return itemMapper.convertListItem(items);
    }

    @Override
    public ItemDto create(ItemDto itemDto, int userId) {
        User user = userStorage.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользоваетля с id " + userId + " не существует");
        }
        itemDto.setOwner(userMapper.convertUser(user));
        Item newItem = itemMapper.convertItemDto(itemDto);
        return itemMapper.convertItem(itemStorage.add(newItem));
    }

    @Override
    public ItemDto update(ItemDto itemDto, int userId) {
        User user = userStorage.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользоваетля с id " + userId + " не существует");
        }
        itemDto.setOwner(userMapper.convertUser(user));
        Item existentItem = itemStorage.findById(itemDto.getId());
        if (existentItem == null) {
            throw new NotFoundException("Предмета с id " + itemDto.getId() + " не существует");
        }
        Item existentItemCopy = existentItem.toBuilder().build();
        Item newItem = itemMapper.convertItemDto(itemDto);
        if (!Objects.equals(existentItem.getOwner(), user)) {
            throw new NotFoundException("Попытка изменить вещь пользователем, не являющимся владельцом");
        }
        if (newItem.getName() != null) {
            existentItemCopy.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            existentItemCopy.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            existentItemCopy.setAvailable(newItem.getAvailable());
        }

        return itemMapper.convertItem(itemStorage.update(existentItemCopy));
    }

    @Override
    public ItemDto get(int itemId) {
        Item item = itemStorage.findById(itemId);
        if (item == null) {
            throw new NotFoundException("Предмета с id " + itemId + " не существует");
        }
        return itemMapper.convertItem(item);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> items = itemStorage.findByText(text);
        return itemMapper.convertListItem(items);
    }
}
