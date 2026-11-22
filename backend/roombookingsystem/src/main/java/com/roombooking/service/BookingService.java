package com.roombooking.service;

import com.roombooking.exception.BookingConflictException;
import com.roombooking.exception.BookingNotFoundException;
import com.roombooking.exception.InvalidInputException;
import com.roombooking.exception.RoomNotFoundException;
import com.roombooking.model.Booking;
import com.roombooking.model.Room;
import com.roombooking.repository.BookingRepository;
import com.roombooking.repository.RoomRepository;
import com.roombooking.service.params.NewBooking;
import com.roombooking.service.params.ParsedSlot;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business rules for bookings: creating (no double booking), listing, finding and cancelling. */
@Service
public class BookingService {

    private static final int MAX_BOOKED_BY_LENGTH = 100;
    private static final int MAX_PURPOSE_LENGTH = 200;
    private static final String EMPTY_BOOKED_BY = "Booked by must not be empty.";
    private static final String BOOKED_BY_TOO_LONG = "Booked by must be at most 100 characters.";
    private static final String PURPOSE_TOO_LONG = "Purpose must be at most 200 characters.";
    private static final String BOOKING_IN_PAST = "Cannot create a booking in the past.";

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final Clock clock;

    /** Creates the service; the clock decides what "now" is, so tests can fix the time. */
    public BookingService(RoomRepository roomRepository, BookingRepository bookingRepository, Clock clock) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.clock = clock;
    }

    /**
     * Creates a booking after checking every rule in the order of SPEC.md section 9.
     *
     * @param newBooking the room id, person, purpose and time slot as typed by the user
     * @return the saved booking with its new id
     * @throws RoomNotFoundException    if no room has this id
     * @throws InvalidInputException    if a value is invalid or the slot starts in the past
     * @throws BookingConflictException if the room is already booked at this time (BR-9)
     */
    @Transactional
    public Booking createBooking(NewBooking newBooking) {
        Room room = findRoomOrThrow(newBooking.roomId());
        validateBookedBy(newBooking.bookedBy());
        String purpose = normalizePurpose(newBooking.purpose());
        ParsedSlot slot = TimeRules.parseSlot(newBooking.slot());
        ensureNotInPast(slot);
        ensureNoOverlap(room, slot);
        String bookedBy = newBooking.bookedBy().strip();
        return bookingRepository.save(
                new Booking(room.getId(), bookedBy, purpose, slot.date(), slot.start(), slot.end()));
    }

    /**
     * Lists bookings, optionally filtered by room and/or date.
     *
     * @param roomId the room to filter by, or {@code null} for all rooms
     * @param date   the date as "YYYY-MM-DD", or {@code null} for all dates
     * @return the matching bookings sorted by date and start time, or an empty list
     * @throws RoomNotFoundException if a room id is given but no room has it
     * @throws InvalidInputException if the date is given but invalid
     */
    @Transactional(readOnly = true)
    public List<Booking> listBookings(Long roomId, String date) {
        if (roomId != null) {
            findRoomOrThrow(roomId);
        }
        LocalDate bookingDate = (date == null) ? null : TimeRules.parseDate(date);
        if (roomId != null && bookingDate != null) {
            return bookingRepository.findByRoomIdAndBookingDateOrderByStartTimeAsc(roomId, bookingDate);
        }
        if (roomId != null) {
            return bookingRepository.findByRoomIdOrderByBookingDateAscStartTimeAsc(roomId);
        }
        if (bookingDate != null) {
            return bookingRepository.findByBookingDateOrderByStartTimeAscRoomIdAsc(bookingDate);
        }
        return bookingRepository.findAllByOrderByBookingDateAscStartTimeAscRoomIdAsc();
    }

    /**
     * Returns one booking by its id.
     *
     * @param bookingId the id of the booking
     * @return the booking
     * @throws BookingNotFoundException if no booking has this id
     */
    @Transactional(readOnly = true)
    public Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    /**
     * Cancels (deletes) a booking.
     *
     * @param bookingId the id of the booking to cancel
     * @throws BookingNotFoundException if no booking has this id
     */
    @Transactional
    public void cancelBooking(Long bookingId) {
        getBooking(bookingId);
        bookingRepository.deleteById(bookingId);
    }

    private Room findRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
    }

    private void validateBookedBy(String bookedBy) {
        if (bookedBy == null || bookedBy.isBlank()) {
            throw new InvalidInputException(EMPTY_BOOKED_BY);
        }
        if (bookedBy.strip().length() > MAX_BOOKED_BY_LENGTH) {
            throw new InvalidInputException(BOOKED_BY_TOO_LONG);
        }
    }

    private String normalizePurpose(String purpose) {
        String trimmedPurpose = (purpose == null) ? "" : purpose.strip();
        if (trimmedPurpose.length() > MAX_PURPOSE_LENGTH) {
            throw new InvalidInputException(PURPOSE_TOO_LONG);
        }
        return trimmedPurpose;
    }

    private void ensureNotInPast(ParsedSlot slot) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (LocalDateTime.of(slot.date(), slot.start()).isBefore(now)) {
            throw new InvalidInputException(BOOKING_IN_PAST);
        }
    }

    private void ensureNoOverlap(Room room, ParsedSlot slot) {
        List<Booking> overlapping =
                bookingRepository.findOverlapping(room.getId(), slot.date(), slot.start(), slot.end());
        if (overlapping.isEmpty()) {
            return;
        }
        Booking existing = overlapping.getFirst();
        throw new BookingConflictException("Room '" + room.getName() + "' is already booked from "
                + TimeRules.formatTime(existing.getStartTime()) + " to "
                + TimeRules.formatTime(existing.getEndTime()) + " on " + existing.getBookingDate() + ".");
    }
}
