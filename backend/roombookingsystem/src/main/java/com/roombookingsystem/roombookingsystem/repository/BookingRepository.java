package com.roombookingsystem.roombookingsystem.repository;

import com.roombookingsystem.roombookingsystem.model.Booking;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Database access for bookings. Contains queries only, no business rules. */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** All bookings sorted by date, start time and room. */
    List<Booking> findAllByOrderByBookingDateAscStartTimeAscRoomIdAsc();

    /** Bookings of one room sorted by date and start time. */
    List<Booking> findByRoomIdOrderByBookingDateAscStartTimeAsc(Long roomId);

    /** Bookings on one date sorted by start time and room. */
    List<Booking> findByBookingDateOrderByStartTimeAscRoomIdAsc(LocalDate bookingDate);

    /** Bookings of one room on one date sorted by start time. */
    List<Booking> findByRoomIdAndBookingDateOrderByStartTimeAsc(Long roomId, LocalDate bookingDate);

    /**
     * Bookings of this room on this date that overlap the range [startTime, endTime).
     * Two ranges overlap when each one starts before the other one ends.
     */
    @Query("""
           SELECT b FROM Booking b
           WHERE b.roomId = :roomId
             AND b.bookingDate = :date
             AND b.startTime < :endTime
             AND b.endTime > :startTime
           ORDER BY b.startTime
           """)
    List<Booking> findOverlapping(@Param("roomId") Long roomId,
                                  @Param("date") LocalDate date,
                                  @Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime);

    /** Ids of all rooms that have at least one booking overlapping the given slot. */
    @Query("""
           SELECT DISTINCT b.roomId FROM Booking b
           WHERE b.bookingDate = :date
             AND b.startTime < :endTime
             AND b.endTime > :startTime
           """)
    Set<Long> findBookedRoomIds(@Param("date") LocalDate date,
                                @Param("startTime") LocalTime startTime,
                                @Param("endTime") LocalTime endTime);

    /** Returns true if the room has at least one booking. */
    boolean existsByRoomId(Long roomId);
}
