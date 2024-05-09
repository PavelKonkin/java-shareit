package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @EntityGraph(value = "booking.item.user")
    @Query(" select b from Booking b where b.id = ?1 and (b.booker.id = ?2 or b.item.owner.id = ?2)")
    Optional<Booking> getByIdAndBookerIdOrItemOwnerId(long bookingId, long userId);

    @EntityGraph(value = "booking.item.user")
    List<Booking> findAllByBookerId(long bookerId, Pageable page);

    @EntityGraph(value = "booking.item.user")
    List<Booking> findAllByBookerIdAndStartDateAfter(long bookerId, LocalDateTime currentDate, Pageable page);

    @EntityGraph(value = "booking.item.user")
    List<Booking> findAllByBookerIdAndEndDateIsBefore(long bookerId, LocalDateTime currentDate, Pageable page);

    @EntityGraph(value = "booking.item.user")
    @Query(" select b from Booking b where b.booker.id = ?1 and ?2 between b.startDate and b.endDate")
    List<Booking> findAllByBookerCurrent(long bookerId, LocalDateTime currentDate, Pageable page);

    @EntityGraph(value = "booking.item.user")
    List<Booking> findAllByBookerIdAndStatus(long bookerId, BookingState state, Pageable page);

    @EntityGraph(value = "booking.item.user")
    List<Booking> findAllByItemOwnerId(long ownerId, Pageable page);

    @EntityGraph(value = "booking.item.user")
    List<Booking> findAllByItemOwnerIdAndStartDateAfter(long ownerId, LocalDateTime currentDate, Pageable page);

    @EntityGraph(value = "booking.item.user")
    List<Booking> findAllByItemOwnerIdAndEndDateIsBefore(long ownerId, LocalDateTime currentDate, Pageable page);

    @EntityGraph(value = "booking.item.user")
    @Query(" select b from Booking b where b.item.owner.id = ?1" +
            " and ?2 between b.startDate and b.endDate")
    List<Booking> findAllByOwnerCurrent(long ownerId, LocalDateTime currentDate, Pageable page);

    @EntityGraph(value = "booking.item.user")
    List<Booking> findAllByItemOwnerIdAndStatus(long ownerId, BookingState bookingState, Pageable page);

    @EntityGraph(value = "booking.item.user")
    List<Booking> findAllByItemId(long itemId);

    @EntityGraph(value = "booking.item.user")
    Optional<Booking> findFirstByBookerIdAndItemIdAndStatusAndEndDateIsBefore(long bookerId, long itemId,
                                                                              BookingState bookingState,
                                                                              LocalDateTime created);

    @EntityGraph(value = "booking.item.user")
    @Query (" select b from Booking b where b.item.id in ?1")
    List<Booking> findAllByItemsId(List<Long> itemsId);
}
