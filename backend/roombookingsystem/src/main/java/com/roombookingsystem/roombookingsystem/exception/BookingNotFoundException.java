package com.roombookingsystem.roombookingsystem.exception;

/** Thrown when no booking exists with the given id. */
public class BookingNotFoundException extends NotFoundException {

    /** Creates the error for the missing booking id. */
    public BookingNotFoundException(Long bookingId) {
        super("BOOKING_NOT_FOUND", "Booking with id " + bookingId + " was not found.");
    }
}
