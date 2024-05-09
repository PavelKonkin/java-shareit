package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    private ItemRequest itemRequest;
    private ItemRequest itemRequest2;
    private final Sort sort = Sort.by("id");
    private final PageRequest page = PageRequest.of(0, 10, sort);

    @BeforeEach
    void setup() {
        user1 = User.builder()
                .name("test user")
                .email("test@user.email")
                .build();
        userRepository.save(user1);
        user2 = User.builder()
                .name("test user2")
                .email("test@user2.email")
                .build();
        userRepository.save(user2);
        itemRequest = ItemRequest.builder()
                .requester(user2)
                .created(LocalDateTime.now())
                .description("request description")
                .build();
        itemRequestRepository.save(itemRequest);
        itemRequest2 = ItemRequest.builder()
                .requester(user1)
                .created(LocalDateTime.now())
                .description("request description2")
                .build();
        itemRequestRepository.save(itemRequest2);
        item1 = Item.builder()
                .name("test item")
                .description("test description")
                .available(true)
                .request(itemRequest)
                .owner(user1)
                .build();
        itemRepository.save(item1);
        item2 = Item.builder()
                .name("test item2")
                .description("test description2")
                .available(true)
                .request(itemRequest2)
                .owner(user1)
                .build();
        itemRepository.save(item2);
    }

    @Test
    void findAllByOwnerId_whenThereAreItems_thenReturnListOfItems() {
        List<Item> actualItemList = itemRepository.findAllByOwnerId(user1.getId(), page);

        assertThat(List.of(item1, item2), is(actualItemList));
    }

    @Test
    void findAllByOwnerId_whenNoItems_thenReturnEmptyList() {
        List<Item> actualItemList = itemRepository.findAllByOwnerId(user2.getId(), page);

        assertThat(List.of(), is(actualItemList));
    }

    @Test
    void search_whenFound_thenReturnListOfItems() {
        String text = "test";

        List<Item> actualItemList = itemRepository.search(text, page);

        assertThat(List.of(item1, item2), is(actualItemList));
    }

    @Test
    void search_whenNotFound_thenReturnEmptyList() {
        String text = "cccccc";

        List<Item> actualItemList = itemRepository.search(text, page);

        assertThat(List.of(), is(actualItemList));
    }

    @Test
    void findAllByRequestsId_whenFound_thenReturnListOfItems() {
        List<Item> actualListOfItems
                = itemRepository.findAllByRequestsId(List.of(itemRequest.getId(), itemRequest2.getId()));

        assertThat(List.of(item1, item2), is(actualListOfItems));
    }

    @Test
    void findAllByRequestsId_whenNotFound_thenReturnEmptyList() {
        List<Item> actualListOfItems = itemRepository.findAllByRequestsId(List.of(3L));

        assertThat(List.of(), is(actualListOfItems));
    }

    @Test
    void findAllByRequestId_whenFound_thenReturnListOfItems() {
        List<Item> actualListOfItems = itemRepository.findAllByRequestId(itemRequest.getId());

        assertThat(List.of(item1), is(actualListOfItems));
    }

    @Test
    void findAllByRequestId_whenNotFound_thenReturnEmptyList() {
        List<Item> actualListOfItems = itemRepository.findAllByRequestId(Integer.MAX_VALUE);

        assertThat(List.of(), is(actualListOfItems));
    }

}
