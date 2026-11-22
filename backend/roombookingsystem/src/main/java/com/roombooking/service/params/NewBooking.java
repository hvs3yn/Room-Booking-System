package com.roombooking.service.params;

/**
 * Everything needed to create a booking, exactly as the user typed it.
 * The values are checked by {@code BookingService.createBooking}.
 *
 * @param roomId   the id of the room to book
 * @param bookedBy the name of the person who books
 * @param purpose  an optional purpose, may be {@code null}
 * @param slot     the date and time range of the booking
 */
public record NewBooking(Long roomId, String bookedBy, String purpose, TimeSlot slot) {
}
