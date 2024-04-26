package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemMapper {
    private final UserMapper userMapper;

    @Autowired
    public ItemMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

//    public List<Item> convertListItemDto(List<ItemDto> list) {
//        return list.stream()
//                .map(this::convertItemDto)
//                .collect(Collectors.toList());
//    }

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
                .owner(userMapper.convertUserDto(itemDto.getOwner()))
                .requestId(itemDto.getRequestId())
                .build();
    }

    public ItemDto convertItem(Item item) {

        return ItemDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .id(item.getId())
                .available(item.getAvailable())
                .owner(userMapper.convertUser(item.getOwner()))
                .requestId(item.getRequestId())
                .build();
    }

//    public List<ItemWithBookingsAndCommentsDto> convertListItemToBookingDto(List<Item> items) {
//        return items.stream()
//                .map(this::convertItemToBookingDto)
//                .collect(Collectors.toList());
//    }

    public ItemWithBookingsAndCommentsDto convertItemToBookingDto(Item item) {

        return ItemWithBookingsAndCommentsDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .id(item.getId())
                .available(item.getAvailable())
                .owner(userMapper.convertUser(item.getOwner()))
                .build();
    }
}
