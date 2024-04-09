package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final CommentService commentService;

    @Autowired
    public ItemController(ItemService itemService, CommentService commentService) {
        this.itemService = itemService;
        this.commentService = commentService;
    }

    @GetMapping
    public List<ItemWithBookingsAndCommentsDto> getItems(@NotEmpty @RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Получен запрос на получение списка предметов пользователя с id {}", userId);
        List<ItemWithBookingsAndCommentsDto> result = itemService.getAll(userId);
        log.info("Найден список предметов пользователя с id {}: {}", userId, result);
        return result;
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsAndCommentsDto get(@PathVariable int itemId, @NotEmpty @RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Получен запрос на получение предмета с id {}", itemId);
        ItemWithBookingsAndCommentsDto result = itemService.get(itemId, userId);
        log.info("Найден предмет {}:",  result);
        return result;
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("Получен запрос на получение списка предметов по запросу {}", text);
        List<ItemDto> result = itemService.search(text);
        log.info("Найден список предметов : {}", result);
        return result;

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@Valid @RequestBody ItemDto itemDto,
                          @NotEmpty @RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Получен запрос на создание предмета {} с id пользоателя {}", itemDto, userId);
        ItemDto result = itemService.create(itemDto, userId);
        log.info("Создан предмет {}:",  result);
        return result;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto,
                          @NotEmpty @RequestHeader("X-Sharer-User-Id") int userId,
                          @PathVariable int itemId) {
        log.info("Получен запрос на обновление предмета {} с id пользоателя {}", itemDto, userId);
        itemDto.setId(itemId);
        ItemDto result = itemService.update(itemDto, userId);
        log.info("Обновлен предмет {}:",  result);
        return result;
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto comment(@Valid @RequestBody CommentCreateDto commentCreateDto,
                              @NotEmpty @RequestHeader("X-Sharer-User-Id") int userId,
                              @PathVariable int itemId) {
        commentCreateDto.setItemId(itemId);
        commentCreateDto.setAuthorId(userId);
        log.info("Получен запрос на создание комментария к предмету с id {} пользоателем {}", itemId, userId);
        CommentDto commentDto = commentService.create(commentCreateDto);
        log.info("Создан комментарий {} к предмету с id {} пользоателем {}", commentDto, itemId, userId);
        return commentDto;
    }
}
