package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DataJpaTest
public class CommentRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CommentRepository commentRepository;

    private final Sort sort = Sort.by("created").descending();
    private User user;
    private User user2;
    private Item item;
    private Comment comment;

    @BeforeEach
    void setup() {
        user = User.builder()
                .name("test user")
                .email("test@user.email")
                .build();
        userRepository.save(user);
        user2 = User.builder()
                .name("test user2")
                .email("test@user2.email")
                .build();
        userRepository.save(user2);
        item = Item.builder()
                .name("test item")
                .description("test description")
                .available(true)
                .owner(user)
                .build();
        itemRepository.save(item);
        comment = Comment.builder()
                .text("cpmment text")
                .author(user2)
                .item(item)
                .created(LocalDateTime.now())
                .build();
        commentRepository.save(comment);
    }

    @Test
    void findAllByItemId_whenSuccessful_thenReturnListOfComments() {
        List<Comment> actualListOfComments = commentRepository.findAllByItemId(item.getId(), sort);

        assertThat(List.of(comment), is(actualListOfComments));
    }

    @Test
    void findAllByItemId_whenNotFound_thenReturnEmptyList() {
        List<Comment> actualListOfComments = commentRepository.findAllByItemId(66L, sort);

        assertThat(List.of(), is(actualListOfComments));
    }

    @Test
    void findAllByRequestsId_whenFound_thenReturnListOfItems() {
        List<Comment> actualListOfComments = commentRepository.findAllByItemsId(List.of(item.getId()));

        assertThat(List.of(comment), is(actualListOfComments));
    }

    @Test
    void findAllByRequestsId_whenNotFound_thenReturnEmptyList() {
        List<Comment> actualListOfComments = commentRepository.findAllByItemsId(List.of(66L));

        assertThat(List.of(), is(actualListOfComments));
    }
}
