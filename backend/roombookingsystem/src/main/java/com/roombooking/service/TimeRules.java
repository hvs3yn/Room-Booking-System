package com.roombooking.service;

import com.roombooking.exception.InvalidInputException;
import com.roombooking.service.params.ParsedSlot;
import com.roombooking.service.params.TimeRange;
import com.roombooking.service.params.TimeSlot;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Parses and validates booking dates and times (opening hours, formats, overlaps). */
public final class TimeRules {

    /** Earliest time a booking may start. */
    public static final LocalTime OPENING_TIME = LocalTime.of(8, 0);

    /** Latest time a booking may end. */
    public static final LocalTime CLOSING_TIME = LocalTime.of(22, 0);

    /** Format used for times in messages and JSON, e.g. "09:30". */
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final String DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final String TIME_PATTERN = "^([01]\\d|2[0-3]):[0-5]\\d$";

    private static final String INVALID_DATE = "Date must be a valid date in YYYY-MM-DD format.";
    private static final String INVALID_TIME = "Time must be in HH:mm format (24-hour).";
    private static final String START_NOT_BEFORE_END = "Start time must be before end time.";
    private static final String OUTSIDE_OPENING_HOURS =
            "Bookings are allowed only between 08:00 and 22:00.";

    private TimeRules() {
    }

    /** Parses a "YYYY-MM-DD" string into a real calendar date (BR-4). */
    public static LocalDate parseDate(String value) {
        if (value == null || !value.matches(DATE_PATTERN)) {
            throw new InvalidInputException(INVALID_DATE);
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new InvalidInputException(INVALID_DATE);
        }
    }

    /** Parses a 24-hour "HH:mm" string into a time (BR-5). */
    public static LocalTime parseTime(String value) {
        if (value == null || !value.matches(TIME_PATTERN)) {
            throw new InvalidInputException(INVALID_TIME);
        }
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException ex) {
            throw new InvalidInputException(INVALID_TIME);
        }
    }

    /** Parses the date and both times of a slot and checks the time range (BR-4 to BR-7). */
    public static ParsedSlot parseSlot(TimeSlot slot) {
        LocalDate date = parseDate(slot.date());
        LocalTime start = parseTime(slot.startTime());
        LocalTime end = parseTime(slot.endTime());
        validateTimeRange(start, end);
        return new ParsedSlot(date, start, end);
    }

    /** Checks that start is before end and both are within opening hours (BR-6, BR-7). */
    public static void validateTimeRange(LocalTime start, LocalTime end) {
        if (!start.isBefore(end)) {
            throw new InvalidInputException(START_NOT_BEFORE_END);
        }
        if (start.isBefore(OPENING_TIME) || end.isAfter(CLOSING_TIME)) {
            throw new InvalidInputException(OUTSIDE_OPENING_HOURS);
        }
    }

    /** Returns true if the two ranges share at least one minute; touching ranges do not overlap (BR-9). */
    public static boolean intervalsOverlap(TimeRange first, TimeRange second) {
        return first.start().isBefore(second.end()) && second.start().isBefore(first.end());
    }

    /** Formats a time as "HH:mm", e.g. "09:30". */
    public static String formatTime(LocalTime time) {
        return TIME_FORMAT.format(time);
    }
}
