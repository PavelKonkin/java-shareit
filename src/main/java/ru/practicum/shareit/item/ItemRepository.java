package ru.practicum.shareit.item;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findAllByOwnerId(int ownerId, Pageable page);

    @Query(" select i from Item i " +
        "where upper(i.name) like upper(concat('%', ?1, '%')) " +
        "   or upper(i.description) like upper(concat('%', ?1, '%'))" +
            "and i.available = true ")
    List<Item> search(String text, Pageable page);

    @Query(" select i from Item i where i.request.id in ?1")
    List<Item> findAllByRequestsId(List<Integer> requestsId);

    List<Item> findAllByRequestId(int requestId);
}
