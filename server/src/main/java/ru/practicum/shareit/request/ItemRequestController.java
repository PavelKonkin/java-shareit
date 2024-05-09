package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constant.Constants;
import ru.practicum.shareit.page.OffsetPage;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private final Sort sort = Sort.by("created");

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(@RequestHeader(Constants.USER_HEADER) long userId,
                                 @RequestBody ItemRequestCreateDto requestCreateDto) {
        log.info("Получен запрос на создание запроса {} с id пользоателя {}", requestCreateDto, userId);
        requestCreateDto.setRequesterId(userId);
        ItemRequestDto itemRequestDto = itemRequestService.create(requestCreateDto);
        log.info("Создан запрос {}:",  itemRequestDto);
        return itemRequestDto;
    }

    @GetMapping
    public List<ItemRequestDto> getAllOwn(@RequestHeader(Constants.USER_HEADER) long userId) {
        log.info("Получен запрос на список запросов пользоателя с id {}", userId);
        List<ItemRequestDto> result = itemRequestService.getAllOwn(userId, sort);
        log.info("Получен список запросов пользователя с id {}: {}", userId, result);
        return result;
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader(Constants.USER_HEADER) long userId,
                                       @RequestParam int from,
                                       @RequestParam int size) {
        log.info("Получен запрос на список запросов начиная с позиции {}," +
                " по {} запросов на странице от пользоателя с id {}", from, size, userId);
        Pageable page = new OffsetPage(from, size, sort);
        List<ItemRequestDto> result = itemRequestService.getAll(userId, page);
        log.info("Получен список запросов пользователя с id {}: {}", userId, result);
        return result;
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto get(@RequestHeader(Constants.USER_HEADER) long userId,
                              @PathVariable long requestId) {
        log.info("Получен запрос на запрос с id {} от пользоателя с id {}", requestId, userId);
        ItemRequestDto result = itemRequestService.get(userId, requestId);
        log.info("Получен запрос {} для пользователя с id {}", result, userId);
        return result;
    }
}
