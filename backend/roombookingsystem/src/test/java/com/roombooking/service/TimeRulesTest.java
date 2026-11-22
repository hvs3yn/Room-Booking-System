package com.roombooking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.roombooking.exception.InvalidInputException;
import com.roombooking.service.params.ParsedSlot;
import com.roombooking.service.params.TimeRange;
import com.roombooking.service.params.TimeSlot;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TimeRulesTest {

    private static final String INVALID_DATE = "Date must be a valid date in YYYY-MM-DD format.";
    private static final String INVALID_TIME = "Time must be in HH:mm format (24-hour).";
    private static final String START_NOT_BEFORE_END = "Start time must be before end time.";
    private static final String OUTSIDE_OPENING_HOURS = "Bookings are allowed only between 08:00 and 22:00.";

    @Test
    @DisplayName("parseDate turns a valid YYYY-MM-DD string into a LocalDate")
    void parseDate_validDate_returnsLocalDate() {
        assertThat(TimeRules.parseDate("2030-01-15")).isEqualTo(LocalDate.of(2030, 1, 15));
    }

    @ParameterizedTest(name = "parseDate rejects \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"2030-02-30", "2030-13-01", "30-01-2030", "2030-1-5", "   ", "abc"})
    @DisplayName("parseDate rejects missing, badly formatted and impossible dates")
    void parseDate_invalidDate_throws(String value) {
        assertThatThrownBy(() -> TimeRules.parseDate(value))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage(INVALID_DATE);
    }

    @ParameterizedTest(name = "parseTime accepts \"{0}\"")
    @ValueSource(strings = {"08:00", "21:59", "00:00", "23:59"})
    @DisplayName("parseTime accepts valid 24-hour HH:mm times")
    void parseTime_validTime_returnsLocalTime(String value) {
        assertThat(TimeRules.parseTime(value)).isEqualTo(LocalTime.parse(value));
    }

    @ParameterizedTest(name = "parseTime rejects \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"9:00", "24:00", "10:60", "10:00:00", "abc", " 10:00"})
    @DisplayName("parseTime rejects missing and badly formatted times")
    void parseTime_invalidTime_throws(String value) {
        assertThatThrownBy(() -> TimeRules.parseTime(value))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage(INVALID_TIME);
    }

    @ParameterizedTest(name = "{0}-{1} is rejected")
    @CsvSource({"10:00, 10:00", "11:00, 10:00"})
    @DisplayName("validateTimeRange rejects a start that is not before the end")
    void validateTimeRange_startNotBeforeEnd_throws(String start, String end) {
        assertThatThrownBy(() -> TimeRules.validateTimeRange(LocalTime.parse(start), LocalTime.parse(end)))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage(START_NOT_BEFORE_END);
    }

    @ParameterizedTest(name = "{0}-{1} is rejected")
    @CsvSource({"07:59, 09:00", "21:00, 22:01"})
    @DisplayName("validateTimeRange rejects ranges outside 08:00-22:00")
    void validateTimeRange_outsideOpeningHours_throws(String start, String end) {
        assertThatThrownBy(() -> TimeRules.validateTimeRange(LocalTime.parse(start), LocalTime.parse(end)))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage(OUTSIDE_OPENING_HOURS);
    }

    @Test
    @DisplayName("validateTimeRange accepts the full opening hours 08:00-22:00")
    void validateTimeRange_fullOpeningHours_isAccepted() {
        assertThatCode(() -> TimeRules.validateTimeRange(LocalTime.of(8, 0), LocalTime.of(22, 0)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "{0}-{1} and {2}-{3} overlap: {4}")
    @CsvSource({
        "10:00, 11:00, 10:30, 11:30, true",   // partial overlap
        "10:00, 12:00, 10:30, 11:00, true",   // second inside first
        "10:30, 11:00, 10:00, 12:00, true",   // first inside second
        "10:00, 11:00, 10:00, 11:00, true",   // identical
        "10:00, 11:00, 11:00, 12:00, false",  // touching
        "10:00, 11:00, 13:00, 14:00, false"   // separate
    })
    @DisplayName("intervalsOverlap follows the rule start1 < end2 AND start2 < end1")
    void intervalsOverlap_followsOverlapRule(String startA, String endA, String startB, String endB,
                                             boolean expected) {
        TimeRange first = new TimeRange(LocalTime.parse(startA), LocalTime.parse(endA));
        TimeRange second = new TimeRange(LocalTime.parse(startB), LocalTime.parse(endB));

        assertThat(TimeRules.intervalsOverlap(first, second)).isEqualTo(expected);
    }

    @Test
    @DisplayName("parseSlot parses the date and both times of a valid slot")
    void parseSlot_validSlot_returnsParsedSlot() {
        ParsedSlot parsed = TimeRules.parseSlot(new TimeSlot("2030-01-15", "10:00", "11:30"));

        assertThat(parsed).isEqualTo(new ParsedSlot(LocalDate.of(2030, 1, 15), LocalTime.of(10, 0),
                LocalTime.of(11, 30)));
    }

    @Test
    @DisplayName("parseSlot rejects a slot whose start is after its end")
    void parseSlot_reversedRange_throws() {
        assertThatThrownBy(() -> TimeRules.parseSlot(new TimeSlot("2030-01-15", "12:00", "11:00")))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage(START_NOT_BEFORE_END);
    }

    @Test
    @DisplayName("formatTime writes a time as HH:mm with leading zeros")
    void formatTime_writesHoursAndMinutes() {
        assertThat(TimeRules.formatTime(LocalTime.of(9, 5))).isEqualTo("09:05");
    }
}
