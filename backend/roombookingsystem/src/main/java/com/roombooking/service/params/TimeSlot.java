package com.roombooking.service.params;

/**
 * A date and a time range, exactly as the user typed them.
 * The values are checked by {@code TimeRules} inside the services.
 *
 * @param date      the date as "YYYY-MM-DD"
 * @param startTime the start time as "HH:mm"
 * @param endTime   the end time as "HH:mm"
 */
public record TimeSlot(String date, String startTime, String endTime) {
}
