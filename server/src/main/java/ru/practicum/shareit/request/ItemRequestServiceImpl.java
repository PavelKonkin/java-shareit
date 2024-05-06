package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Autowired
    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository, UserRepository userRepository,
                              ItemRequestMapper itemRequestMapper, ItemRepository itemRepository,
                                  ItemMapper itemMapper) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
        this.itemRequestMapper = itemRequestMapper;
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }

    @Override
    public ItemRequestDto create(ItemRequestCreateDto requestCreateDto) {
        long userId = requestCreateDto.getRequesterId();
        User user = getAndCheckUserExistence(userId);
        ItemRequest itemRequest = ItemRequest.builder()
                .created(requestCreateDto.getCreated())
                .requester(user)
                .description(requestCreateDto.getDescription())
                .build();
        return itemRequestMapper.convertRequest(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> getAllOwn(long userId, Sort sort) {
        getAndCheckUserExistence(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterId(userId, sort);
        return composeItemRequestDtoList(itemRequests);
    }

    @Override
    public List<ItemRequestDto> getAll(long userId, Pageable page) {
        getAndCheckUserExistence(userId);
        List<ItemRequest> itemRequestList = itemRequestRepository.findAllByRequesterIdIsNot(userId, page);
        return composeItemRequestDtoList(itemRequestList);
    }

    @Override
    public ItemRequestDto get(long userId, long requestId) {
        getAndCheckUserExistence(userId);

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запроса с id " + requestId + " не существует"));

        List<Item> items = itemRepository.findAllByRequestId(requestId);
        List<ItemDto> itemDtos = itemMapper.convertListItem(items);

        ItemRequestDto itemRequestDto = itemRequestMapper.convertRequest(itemRequest);

        return itemRequestDto.toBuilder().items(itemDtos).build();
    }

    private User getAndCheckUserExistence(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует"));
    }

    private List<ItemRequestDto> composeItemRequestDtoList(List<ItemRequest> itemRequests) {
        List<Long> requestsId = itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());
        List<Item> items = itemRepository.findAllByRequestsId(requestsId);
        Map<Long, List<Item>> itemsByRequest = items.stream()
                .collect(Collectors.groupingBy(el -> el.getRequest().getId()));

        List<ItemRequestDto> itemsRequestsDto = new ArrayList<>();
        for (ItemRequest itemRequest : itemRequests) {
            ItemRequestDto itemRequestDto = itemRequestMapper.convertRequest(itemRequest);
            long requestId = itemRequest.getId();

            List<Item> requestItems = itemsByRequest.getOrDefault(requestId, List.of());

            itemsRequestsDto.add(itemRequestDto.toBuilder()
                    .items(itemMapper.convertListItem(requestItems))
                    .build());
        }
        return itemsRequestsDto;
    }
}
