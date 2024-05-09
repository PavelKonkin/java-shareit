package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(ItemRequestCreateDto requestCreateDto);

    List<ItemRequestDto> getAllOwn(long userId, Sort sort);

    List<ItemRequestDto> getAll(long userId, Pageable page);

    ItemRequestDto get(long userId, long requestId);
}
