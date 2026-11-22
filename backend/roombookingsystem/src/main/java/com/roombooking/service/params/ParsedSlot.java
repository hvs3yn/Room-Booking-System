package com.roombooking.service.params;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A date and a time range that are already parsed and checked (BR-4 to BR-7).
 * Created by {@code TimeRules.parseSlot} from a {@link TimeSlot}.
 *
 * @param date  the date of the slot
 * @param start the start time, before {@code end}
 * @param end   the end time, after {@code start}
 */
public record ParsedSlot(LocalDate date, LocalTime start, LocalTime end) {
}
