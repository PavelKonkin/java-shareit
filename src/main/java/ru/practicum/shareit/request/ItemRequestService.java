package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(ItemRequestCreateDto requestCreateDto);

    List<ItemRequestDto> getAllOwn(int userId);

    List<ItemRequestDto> getAll(int userId, int from, int size);

    ItemRequestDto get(int userId, int requestId);
}
