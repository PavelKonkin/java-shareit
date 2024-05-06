package ru.practicum.shareit.item;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;


public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(value = "comment.item.owner")
    List<Comment> findAllByItemId(long itemId, Sort sort);

    @EntityGraph(value = "comment.item.owner")
    @Query(" select c from Comment  c where c.item.id in ?1")
    List<Comment> findAllByItemsId(List<Long> itemsId);
}
