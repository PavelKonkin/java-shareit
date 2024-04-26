package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    @EntityGraph(value = "booking.item.user")
    @Query(" select b from Booking b where b.id = ?1 and (b.booker.id = ?2 or b.item.owner.id = ?2)")
    Optional<Booking> getByIdAndBookerIdOrItemOwnerId(int bookingId, int userId);

    @EntityGraph(value = "booking.item.user")
    Page<Booking> findAllByBookerId(int bookerId, Pageable page);

    @EntityGraph(value = "booking.item.user")
    Page<Booking> findAllByBookerIdAndStartDateAfter(int bookerId, LocalDateTime currentDate, Pageable page);

    @EntityGraph(value = "booking.item.user")
    Page<Booking> findAllByBookerIdAndEndDateIsBefore(int bookerId, LocalDateTime currentDate, Pageable page);

    @EntityGraph(value = "booking.item.user")
    @Query(" select b from Booking b where b.booker.id = ?1 and ?2 between b.startDate and b.endDate" +
            " order by b.id")
    Page<Booking> findAllByBookerCurrent(int bookerId, LocalDateTime currentDate, Pageable page);

    @EntityGraph(value = "booking.item.user")
    Page<Booking> findAllByBookerIdAndStatus(int bookerId, BookingState state, Pageable page);

    @EntityGraph(value = "booking.item.user")
    Page<Booking> findAllByItemOwnerId(int ownerId, Pageable page);

    @EntityGraph(value = "booking.item.user")
    Page<Booking> findAllByItemOwnerIdAndStartDateAfter(int ownerId, LocalDateTime currentDate, Pageable page);

    @EntityGraph(value = "booking.item.user")
    Page<Booking> findAllByItemOwnerIdAndEndDateIsBefore(int ownerId, LocalDateTime currentDate, Pageable page);

    @EntityGraph(value = "booking.item.user")
    @Query(" select b from Booking b where b.item.owner.id = ?1" +
            " and ?2 between b.startDate and b.endDate order by b.id")
    Page<Booking> findAllByOwnerCurrent(int ownerId, LocalDateTime currentDate, Pageable page);

    @EntityGraph(value = "booking.item.user")
    Page<Booking> findAllByItemOwnerIdAndStatus(int ownerId, BookingState bookingState, Pageable page);

    @EntityGraph(value = "booking.item.user")
    List<Booking> findAllByItemId(int itemId);

    @EntityGraph(value = "booking.item.user")
    Optional<Booking> findFirstByBookerIdAndItemIdAndStatusAndEndDateIsBefore(int bookerId, int itemId,
                                                               BookingState bookingState, LocalDateTime created);

    @EntityGraph(value = "booking.item.user")
    @Query (" select b from Booking b where b.item.id in ?1")
    List<Booking> findAllByItemsId(List<Integer> itemsId);
}
