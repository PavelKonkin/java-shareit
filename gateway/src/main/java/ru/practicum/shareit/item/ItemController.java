package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constant.Constants;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@Slf4j
@RequestMapping(value = "/items")
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @Autowired
    public ItemController(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(@PathVariable long itemId,
                              @RequestHeader(Constants.USER_HEADER) long userId) {
        log.info("Get item with id {} from user with id {}", itemId, userId);
        return itemClient.get(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllOwn(@RequestHeader(Constants.USER_HEADER) long userId,
                                                          @RequestParam(defaultValue = "0") @Min(0) int from,
                                                          @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Get list of items owned by user with с id {}," +
                " beginning from {}, by {} items on page", userId, from, size);
        return itemClient.getAllOwn(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text,
                                @RequestParam(defaultValue = "0") @Min(0) int from,
                                @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Search items by text {}," +
                " beginning from {}, by {} items on page", text, from, size);
        return itemClient.search(text, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody ItemDto itemDto,
                          @RequestHeader(Constants.USER_HEADER) long userId) {
        log.info("Create item {} by user with id {}", itemDto, userId);
        return itemClient.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestBody ItemDto itemDto,
                          @RequestHeader(Constants.USER_HEADER) long userId,
                          @PathVariable long itemId) {
        log.info("Update item {} by user with id {}", itemDto, userId);
        return itemClient.update(itemDto, userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> comment(@Valid @RequestBody CommentCreateDto commentCreateDto,
                              @RequestHeader(Constants.USER_HEADER) long userId,
                              @PathVariable long itemId) {
        log.info("Comment to item with id {} by user with id {}", itemId, userId);
        return itemClient.comment(commentCreateDto, userId, itemId);
    }
}
