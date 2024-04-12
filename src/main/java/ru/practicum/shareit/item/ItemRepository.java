package ru.practicum.shareit.item;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    @EntityGraph(value = "item.owner")
    List<Item> findAllByOwnerId(int ownerId, Sort sort);

    @EntityGraph(value = "item.owner")
    @Query(" select i from Item i " +
        "where upper(i.name) like upper(concat('%', ?1, '%')) " +
        "   or upper(i.description) like upper(concat('%', ?1, '%'))" +
            "and i.available = true ")
    List<Item> search(String text);
}
