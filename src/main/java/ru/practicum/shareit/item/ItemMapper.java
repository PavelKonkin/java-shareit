package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemMapper {
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
                .build();
    }

    public ItemDto convertItem(Item item) {
        Integer requestId = null;
        if (item.getRequest() != null) {
            requestId = item.getRequest().getId();
        }

        return ItemDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .id(item.getId())
                .available(item.getAvailable())
                .requestId(requestId)
                .build();
    }

    public ItemWithBookingsAndCommentsDto convertItemToBookingDto(Item item) {

        return ItemWithBookingsAndCommentsDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .id(item.getId())
                .available(item.getAvailable())
                .build();
    }
}
