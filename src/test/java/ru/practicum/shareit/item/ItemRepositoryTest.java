package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Transactional
@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    private final Sort sort = Sort.by("id");
    private final PageRequest page = PageRequest.of( 0, 10, sort);

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
        item1 = Item.builder()
                .name("test item")
                .description("test description")
                .available(true)
                .owner(user1)
                .requestId(1)
                .build();
        itemRepository.save(item1);
        item2 = Item.builder()
                .name("test item2")
                .description("test description2")
                .available(true)
                .owner(user1)
                .requestId(2)
                .build();
        itemRepository.save(item2);
    }

    @Test
    void findAllByOwnerId_whenThereAreItems_thenReturnListOfItems() {
        Page<Item> actualItemList = itemRepository.findAllByOwnerId(user1.getId(), page);

        assertThat(List.of(item1, item2), is(actualItemList.toList()));
    }

    @Test
    void findAllByOwnerId_whenNoItems_thenReturnEmptyList() {
        Page<Item> actualItemList = itemRepository.findAllByOwnerId(user2.getId(), page);

        assertThat(List.of(), is(actualItemList.toList()));
    }

    @Test
    void search_whenFound_thenReturnListOfItems() {
        String text = "test";

        Page<Item> actualItemList = itemRepository.search(text, page);

        assertThat(List.of(item1, item2), is(actualItemList.toList()));
    }

    @Test
    void search_whenNotFound_thenReturnEmptyList() {
        String text = "cccccc";

        Page<Item> actualItemList = itemRepository.search(text, page);

        assertThat(List.of(), is(actualItemList.toList()));
    }

    @Test
    void findAllByRequestsId_whenFound_thenReturnListOfItems() {
        List<Item> actualListOfItems = itemRepository.findAllByRequestsId(List.of(1, 2));

        assertThat(List.of(item1, item2), is(actualListOfItems));
    }

    @Test
    void findAllByRequestsId_whenNotFound_thenReturnEmptyList() {
        List<Item> actualListOfItems = itemRepository.findAllByRequestsId(List.of(3));

        assertThat(List.of(), is(actualListOfItems));
    }

    @Test
    void findAllByRequestId_whenFound_thenReturnListOfItems() {
        List<Item> actualListOfItems = itemRepository.findAllByRequestId(1);

        assertThat(List.of(item1), is(actualListOfItems));
    }

    @Test
    void findAllByRequestId_whenNotFound_thenReturnEmptyList() {
        List<Item> actualListOfItems = itemRepository.findAllByRequestId(3);

        assertThat(List.of(), is(actualListOfItems));
    }

}
