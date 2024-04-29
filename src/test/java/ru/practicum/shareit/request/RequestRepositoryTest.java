package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class RequestRepositoryTest {
    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;
    private User testUser1;
    private User testUser2;
    private User testUser3;
    private ItemRequest itemRequest1;
    private ItemRequest itemRequest2;
    private ItemRequest itemRequest3;
    Sort sort;

    @BeforeEach
    void setup() {
        testUser1 = User.builder()
                .name("test name")
                .email("test@email.com")
                .build();
        testUser2 = User.builder()
                .name("test name 2")
                .email("test2@email.com")
                .build();
        testUser3 = User.builder()
                .id(66)
                .name("test name 3")
                .email("test3@email.com")
                .build();
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        itemRequest1 = ItemRequest.builder()
                .description("test description 1")
                .requester(testUser1)
                .created(LocalDateTime.now())
                .build();
        itemRequest2 = ItemRequest.builder()
                .description("test description 2")
                .requester(testUser1)
                .created(LocalDateTime.now())
                .build();
        itemRequest3 = ItemRequest.builder()
                .description("test description 3")
                .requester(testUser3)
                .created(LocalDateTime.now())
                .id(66)
                .build();
        itemRequestRepository.save(itemRequest2);
        sort = Sort.by("created");
    }

    @Test
    public void contextLoads() {
        assertThat(em, notNullValue());
    }

    @Test
    void save_whenSuccessful() {
        itemRequestRepository.save(itemRequest1);

        assertThat(itemRequest1.getId(), notNullValue());
    }

    @Test
    void save_whenNoSuchUserInDB_thenThrownException() {
        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> itemRequestRepository.save(itemRequest3)
        );
        assertThat(exception.getMessage(), containsString("could not execute statement"));
    }

    @Test
    void findAllByRequesterId_whenSuccessful_thenReturnItemRequestDtoList() {
        List<ItemRequest> itemRequestList = itemRequestRepository.findAllByRequesterId(testUser1.getId(), sort);

        assertThat(itemRequestList, iterableWithSize(1));
        assertThat(itemRequestList, contains(itemRequest2));
    }

    @Test
    void findAllByRequesterId_whenNoRecords__thenReturnEmptyItemRequestDtoList() {
        List<ItemRequest> itemRequestList = itemRequestRepository.findAllByRequesterId(testUser2.getId(), sort);

        assertThat(itemRequestList, emptyIterable());
    }

    @Test
    void findAllByRequesterIdIsNot_whenSuccessful_thenReturnItemRequestDtoPage() {
        PageRequest page = PageRequest.of(0, 2, sort);

        List<ItemRequest> actualItemRequestList = itemRequestRepository.findAllByRequesterIdIsNot(testUser2.getId(), page);

        assertThat(actualItemRequestList, iterableWithSize(1));
        assertThat(actualItemRequestList, contains(itemRequest2));
        assertThat(actualItemRequestList, instanceOf(List.class));
    }

    @Test
    void findAllByRequesterIdIsNot_whenNoRecords_thenReturnEmptyItemRequestDtoPage() {
        PageRequest page = PageRequest.of(0, 2, sort);

        List<ItemRequest> actualItemRequestList = itemRequestRepository.findAllByRequesterIdIsNot(testUser1.getId(), page);

        assertThat(actualItemRequestList, emptyIterable());
        assertThat(actualItemRequestList, instanceOf(List.class));
    }

    @Test
    void findById_whenFound_thenReturnOptionalOfItemRequest() {
        Optional<ItemRequest> itemRequest = itemRequestRepository.findById(itemRequest2.getId());

        assertThat(itemRequest.isPresent(), is(true));
        assertThat(itemRequest.get(), is(itemRequest2));
    }

    @Test
    void findById_whenNotFound_thenReturnEmptyOptional() {
        Optional<ItemRequest> itemRequest = itemRequestRepository.findById(itemRequest3.getId());

        assertThat(itemRequest.isEmpty(), is(true));
    }
}
