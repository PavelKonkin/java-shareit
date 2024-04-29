package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constant.Constants;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.page.OffsetPage;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final CommentService commentService;
    private final Sort sort = Sort.by("id");

    @Autowired
    public ItemController(ItemService itemService, CommentService commentService) {
        this.itemService = itemService;
        this.commentService = commentService;
    }

    @GetMapping
    public List<ItemWithBookingsAndCommentsDto> getAllOwn(@RequestHeader(Constants.USER_HEADER) int userId,
                                                          @RequestParam(defaultValue = "0") @Min(0) int from,
                                                          @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Получен запрос на получение списка предметов пользователя с id {}," +
                " начиная с {}, по {} предметов на странице", userId, from, size);
        Pageable page = new OffsetPage(from, size, sort);
        List<ItemWithBookingsAndCommentsDto> result = itemService.getAll(userId, page);
        log.info("Найден список предметов пользователя с id {}: {}", userId, result);
        return result;
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsAndCommentsDto get(@PathVariable int itemId,
                                              @RequestHeader(Constants.USER_HEADER) int userId) {
        log.info("Получен запрос на получение предмета с id {}", itemId);
        ItemWithBookingsAndCommentsDto result = itemService.get(itemId, userId);
        log.info("Найден предмет {}:",  result);
        return result;
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text,
                                @RequestParam(defaultValue = "0") @Min(0) int from,
                                @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Получен запрос на получение списка предметов по запросу {}," +
                " начиная с {}, по {} предметов на странице", text, from, size);
        Pageable page = new OffsetPage(from, size, sort);
        List<ItemDto> result = itemService.search(text, page);
        log.info("Найден список предметов : {}", result);
        return result;

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@Valid @RequestBody ItemDto itemDto,
                          @RequestHeader(Constants.USER_HEADER) int userId) {
        log.info("Получен запрос на создание предмета {} с id пользоателя {}", itemDto, userId);
        ItemDto result = itemService.create(itemDto, userId);
        log.info("Создан предмет {}:",  result);
        return result;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto,
                          @RequestHeader(Constants.USER_HEADER) int userId,
                          @PathVariable int itemId) {
        log.info("Получен запрос на обновление предмета {} с id пользоателя {}", itemDto, userId);
        itemDto.setId(itemId);
        ItemDto result = itemService.update(itemDto, userId);
        log.info("Обновлен предмет {}:",  result);
        return result;
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto comment(@Valid @RequestBody CommentCreateDto commentCreateDto,
                              @RequestHeader(Constants.USER_HEADER) int userId,
                              @PathVariable int itemId) {
        commentCreateDto.setItemId(itemId);
        commentCreateDto.setAuthorId(userId);
        log.info("Получен запрос на создание комментария к предмету с id {} пользоателем {}", itemId, userId);
        CommentDto commentDto = commentService.create(commentCreateDto);
        log.info("Создан комментарий {} к предмету с id {} пользоателем {}", commentDto, itemId, userId);
        return commentDto;
    }
}
