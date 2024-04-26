package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
        int userId = requestCreateDto.getRequesterId();
        User user = getAndCheckUserExistence(userId);
        ItemRequest itemRequest = ItemRequest.builder()
                .created(requestCreateDto.getCreated())
                .requester(user)
                .description(requestCreateDto.getDescription())
                .build();
        return itemRequestMapper.convertRequest(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> getAllOwn(int userId) {
        getAndCheckUserExistence(userId);
        Sort sort = Sort.by("created");
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterId(userId, sort);
        return composeItemRequestDtoList(itemRequests);
    }

    @Override
    public List<ItemRequestDto> getAll(int userId, int from, int size) {
        getAndCheckUserExistence(userId);
        Sort sort = Sort.by("created");
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size, sort);
        Page<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdIsNot(userId, page);
        List<ItemRequest> itemRequestList = itemRequests.toList();
        return composeItemRequestDtoList(itemRequestList);
    }

    @Override
    public ItemRequestDto get(int userId, int requestId) {
        getAndCheckUserExistence(userId);

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запроса с id " + requestId + " не существует"));

        List<Item> items = itemRepository.findAllByRequestId(requestId);
        List<ItemDto> itemDtos = itemMapper.convertListItem(items);

        ItemRequestDto itemRequestDto = itemRequestMapper.convertRequest(itemRequest);

        return itemRequestDto.toBuilder().items(itemDtos).build();
    }

    private User getAndCheckUserExistence(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id " + userId + " не существует"));
    }

    private List<ItemRequestDto> composeItemRequestDtoList(List<ItemRequest> itemRequests) {
        List<Integer> requestsId = itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());
        List<Item> items = itemRepository.findAllByRequestsId(requestsId);
        Map<Integer, List<Item>> itemsByRequest = items.stream()
                .collect(Collectors.groupingBy(Item::getRequestId));

        List<ItemRequestDto> itemsRequestsDto = new ArrayList<>();
        for (ItemRequest itemRequest : itemRequests) {
            ItemRequestDto itemRequestDto = itemRequestMapper.convertRequest(itemRequest);
            int requestId = itemRequest.getId();

            List<Item> requestItems = itemsByRequest.getOrDefault(requestId, List.of());

            itemsRequestsDto.add(itemRequestDto.toBuilder()
                    .items(itemMapper.convertListItem(requestItems))
                    .build());
        }
        return itemsRequestsDto;
    }
}