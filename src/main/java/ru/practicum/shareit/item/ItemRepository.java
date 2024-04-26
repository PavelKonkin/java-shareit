package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    @EntityGraph(value = "item.owner")
    Page<Item> findAllByOwnerId(int ownerId, Pageable page);

    @EntityGraph(value = "item.owner")
    @Query(" select i from Item i " +
        "where upper(i.name) like upper(concat('%', ?1, '%')) " +
        "   or upper(i.description) like upper(concat('%', ?1, '%'))" +
            "and i.available = true ")
    Page<Item> search(String text, Pageable page);

    @Query(" select i from Item i where i.requestId in ?1")
    List<Item> findAllByRequestsId(List<Integer> requestsId);

    List<Item> findAllByRequestId(int requestId);
}
