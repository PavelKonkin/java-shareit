package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InMemoryItemStorage implements ItemStorage {
    private static int idCounter = 1;

    private final Map<Integer, Item> items = new HashMap<>();
    private final Map<Integer, List<Item>> itemsByOwner = new HashMap<>();

    @Override
    public Item add(Item item) {
        item.setId(idCounter);
        List<Item> owned = itemsByOwner.getOrDefault(item.getOwner().getId(), new ArrayList<>());
        owned.add(item);
        itemsByOwner.put(item.getOwner().getId(), owned);
        items.put(item.getId(), item);
        increaseIdCounter();
        return item;
    }

    @Override
    public Item update(Item item) {
        Item existentItem = items.get(item.getId());
        List<Item> owned = itemsByOwner.get(item.getOwner().getId());
        owned.remove(existentItem);
        owned.add(item);
        itemsByOwner.put(item.getOwner().getId(), owned);

        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item findById(int id) {
        return items.get(id);
    }

    @Override
    public List<Item> findByText(String text) {
        return items.values().stream()
                .filter(item -> (item.getName().toLowerCase().contains(text)
                        || item.getDescription().toLowerCase().contains(text)) && item.getAvailable())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Item> findByOwner(int userid) {
        return List.copyOf(itemsByOwner.get(userid));
    }

    private static void increaseIdCounter() {
        idCounter++;
    }
}
