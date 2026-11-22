package com.roombooking.service.params;

import java.time.LocalTime;

/**
 * A time range inside one day, from {@code start} (included) to {@code end} (not included).
 *
 * @param start the start time
 * @param end   the end time
 */
public record TimeRange(LocalTime start, LocalTime end) {
}
