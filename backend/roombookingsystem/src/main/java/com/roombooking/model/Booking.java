package com.roombooking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

/** A reservation of one room for one time range on one date. Mapped to the {@code bookings} table. */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "booked_by", nullable = false, length = 100)
    private String bookedBy;

    @Column(nullable = false, length = 200)
    private String purpose;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /** Required by JPA; not for application code. */
    protected Booking() {
    }

    /** Creates a new, not yet saved booking (the database assigns the id). */
    public Booking(Long roomId, String bookedBy, String purpose,
                   LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
        this.roomId = roomId;
        this.bookedBy = bookedBy;
        this.purpose = purpose;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /** Returns the database id, or {@code null} before the booking is saved. */
    public Long getId() {
        return id;
    }

    /** Returns the id of the booked room. */
    public Long getRoomId() {
        return roomId;
    }

    /** Returns the name of the person who made the booking. */
    public String getBookedBy() {
        return bookedBy;
    }

    /** Returns the purpose of the booking ({@code ""} when not given). */
    public String getPurpose() {
        return purpose;
    }

    /** Returns the date of the booking. */
    public LocalDate getBookingDate() {
        return bookingDate;
    }

    /** Returns the start time (inclusive). */
    public LocalTime getStartTime() {
        return startTime;
    }

    /** Returns the end time (exclusive). */
    public LocalTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "Booking{id=" + id + ", roomId=" + roomId + ", bookedBy='" + bookedBy
                + "', date=" + bookingDate + ", " + startTime + "-" + endTime + "}";
    }
}
