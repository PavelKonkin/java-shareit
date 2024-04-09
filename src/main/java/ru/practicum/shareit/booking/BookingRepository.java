package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    @Query(" select b from Booking b where b.id = ?1 and (b.booker.id = ?2 or b.item.owner.id = ?2)")
    Optional<Booking> getByIdAndBookerIdOrItemOwnerId(int bookingId, int userId);

    List<Booking> findAllByBookerIdOrderByIdDesc(int userId);

    List<Booking> findAllByBookerIdAndStartDateAfterOrderByIdDesc(int userId, LocalDateTime currentDate);

    List<Booking> findAllByBookerIdAndEndDateIsBeforeOrderByIdDesc(int userId, LocalDateTime currentDate);

    @Query(" select b from Booking b where b.booker.id = ?1 and ?2 between b.startDate and b.endDate" +
            " order by b.startDate desc")
    List<Booking> findAllByBookerCurrent(int userId, LocalDateTime currentDate);

    List<Booking> findAllByBookerIdAndStatusOrderByIdDesc(int userId, BookingState state);

    List<Booking> findAllByItemOwnerIdOrderByIdDesc(int userId);

    List<Booking> findAllByItemOwnerIdAndStartDateAfterOrderByIdDesc(int userId, LocalDateTime currentDate);

    List<Booking> findAllByItemOwnerIdAndEndDateIsBeforeOrderByIdDesc(int userId, LocalDateTime currentDate);

    @Query(" select b from Booking b where b.item.owner.id = ?1" +
            " and ?2 between b.startDate and b.endDate order by b.startDate desc")
    List<Booking> findAllByOwnerCurrent(int userId, LocalDateTime currentDate);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByIdDesc(int userId, BookingState bookingState);

    List<Booking> findAllByItemId(int itemId);

    Optional<Booking> findFirstByBookerIdAndItemIdAndStatusAndEndDateIsBefore(int userId, int itemId,
                                                               BookingState bookingState, LocalDateTime created);
}
