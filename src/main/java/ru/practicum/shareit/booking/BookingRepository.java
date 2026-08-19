package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(
            Long bookerId,
            LocalDateTime start,
            LocalDateTime end,
            Sort sort
    );

    List<Booking> findByBookerIdAndEndBefore(
            Long bookerId,
            LocalDateTime end,
            Sort sort
    );

    List<Booking> findByBookerIdAndStartAfter(
            Long bookerId,
            LocalDateTime start,
            Sort sort
    );

    List<Booking> findByBookerIdAndStatus(
            Long bookerId,
            BookingStatus status,
            Sort sort
    );

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId")
    List<Booking> findAllByOwnerId(@Param("ownerId") Long ownerId, Sort sort);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.start < :now " +
            "AND b.end > :now")
    List<Booking> findAllByOwnerIdAndStartBeforeAndEndAfter(
            @Param("ownerId") Long ownerId,
            @Param("now") LocalDateTime now,
            Sort sort
    );

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.end < :now")
    List<Booking> findAllByOwnerIdAndEndBefore(
            @Param("ownerId") Long ownerId,
            @Param("now") LocalDateTime now,
            Sort sort
    );

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.start > :now")
    List<Booking> findAllByOwnerIdAndStartAfter(
            @Param("ownerId") Long ownerId,
            @Param("now") LocalDateTime now,
            Sort sort
    );

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.status = :status")
    List<Booking> findAllByOwnerIdAndStatus(
            @Param("ownerId") Long ownerId,
            @Param("status") BookingStatus status,
            Sort sort
    );

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start DESC")
    List<Booking> findApprovedByItemIdOrderByStartDesc(@Param("itemId") Long itemId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.booker.id = :bookerId " +
            "AND b.end < :now " +
            "AND b.status = 'APPROVED'")

    boolean existsByItemIdAndBookerIdAndEndBeforeAndStatus(
            @Param("itemId") Long itemId,
            @Param("bookerId") Long bookerId,
            @Param("now") LocalDateTime now
    );
}