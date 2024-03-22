package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public List<ItemDto> getItems(@NotEmpty @RequestHeader("X-Sharer-User-Id") Integer userId) {
        return itemService.getItems(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto get(@PathVariable Integer itemId) throws NotFoundException {
        return itemService.get(itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.searchItems(text.toLowerCase());

    }

    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemDto itemDto,
                          @NotEmpty @RequestHeader("X-Sharer-User-Id") Integer userId)
            throws NotFoundException {
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto,
                          @NotEmpty @RequestHeader("X-Sharer-User-Id") Integer userId,
                          @PathVariable Integer itemId) throws NotFoundException {
        return itemService.update(itemDto, itemId, userId);

    }
}
