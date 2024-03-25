package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemMapper {

    public List<Item> convertListItemDto(List<ItemDto> list) {
        return list.stream()
                .map(this::convertItemDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> convertListItem(List<Item> list) {
        return list.stream()
                .map(this::convertItem)
                .collect(Collectors.toList());
    }

    public Item convertItemDto(ItemDto itemDto) {

        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .id(itemDto.getId())
                .available(itemDto.getAvailable())
                .owner(itemDto.getOwner())
                .build();
    }

    public ItemDto convertItem(Item item) {

        return ItemDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .id(item.getId())
                .available(item.getAvailable())
                .owner(item.getOwner())
                .build();
    }
}
