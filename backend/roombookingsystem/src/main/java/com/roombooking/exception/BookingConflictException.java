package com.roombooking.exception;

/** Thrown when a new booking overlaps an existing booking of the same room. */
public class BookingConflictException extends ConflictException {

    /** Creates the error; the message is built by BookingService and names the existing booking. */
    public BookingConflictException(String message) {
        super("BOOKING_CONFLICT", message);
    }
}
