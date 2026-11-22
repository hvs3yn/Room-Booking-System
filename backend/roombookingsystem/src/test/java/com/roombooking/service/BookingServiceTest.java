package com.roombooking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.roombooking.exception.BookingConflictException;
import com.roombooking.exception.BookingNotFoundException;
import com.roombooking.exception.InvalidInputException;
import com.roombooking.exception.RoomNotFoundException;
import com.roombooking.model.Booking;
import com.roombooking.model.Room;
import com.roombooking.repository.BookingRepository;
import com.roombooking.repository.RoomRepository;
import com.roombooking.service.params.NewBooking;
import com.roombooking.service.params.TimeSlot;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    /** "Now" in every test: 2030-01-15 at 09:00. */
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2030-01-15T09:00:00Z"), ZoneOffset.UTC);
    private static final Long ROOM_ID = 1L;
    private static final LocalDate FUTURE_DATE = LocalDate.of(2030, 2, 1);

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    private BookingService bookingService;
    private Room room;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(roomRepository, bookingRepository, FIXED_CLOCK);
        room = new Room("Lab 101", 30);
        ReflectionTestUtils.setField(room, "id", ROOM_ID);
    }

    // ---------- createBooking: happy path ----------

    @Test
    @DisplayName("createBooking saves a booking with trimmed values and empty purpose when null")
    void createBooking_validInput_savesTrimmedBooking() {
        givenRoomExists();
        givenNoOverlap("10:00", "11:30");
        givenSaveReturnsArgument();

        Booking saved = bookingService.createBooking(newBooking("  Aydan  ", null, "2030-02-01", "10:00", "11:30"));

        assertThat(saved.getRoomId()).isEqualTo(ROOM_ID);
        assertThat(saved.getBookedBy()).isEqualTo("Aydan");
        assertThat(saved.getPurpose()).isEmpty();
        assertThat(saved.getBookingDate()).isEqualTo(FUTURE_DATE);
        assertThat(saved.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(saved.getEndTime()).isEqualTo(LocalTime.of(11, 30));
    }

    @Test
    @DisplayName("createBooking trims the purpose")
    void createBooking_trimsPurpose() {
        givenRoomExists();
        givenNoOverlap("10:00", "11:00");
        givenSaveReturnsArgument();

        Booking saved = bookingService.createBooking(
                newBooking("Aydan", "  Study group  ", "2030-02-01", "10:00", "11:00"));

        assertThat(saved.getPurpose()).isEqualTo("Study group");
    }

    // ---------- createBooking: room ----------

    @Test
    @DisplayName("createBooking checks the room first: unknown room fails before any other check")
    void createBooking_unknownRoom_throwsBeforeOtherChecks() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());
        NewBooking allFieldsInvalid = newBooking("", "x".repeat(201), "bad", "bad", "bad");

        assertThatThrownBy(() -> bookingService.createBooking(allFieldsInvalid))
                .isInstanceOf(RoomNotFoundException.class)
                .hasMessage("Room with id 1 was not found.");
        verifyNoInteractions(bookingRepository);
    }

    // ---------- createBooking: bookedBy and purpose ----------

    @ParameterizedTest(name = "bookedBy \"{0}\" is rejected")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("createBooking rejects a null, empty or blank bookedBy")
    void createBooking_emptyBookedBy_throws(String bookedBy) {
        givenRoomExists();

        assertThatThrownBy(() -> bookingService.createBooking(
                newBooking(bookedBy, null, "2030-02-01", "10:00", "11:00")))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Booked by must not be empty.");
        verifyNoInteractions(bookingRepository);
    }

    @Test
    @DisplayName("createBooking rejects a bookedBy longer than 100 characters")
    void createBooking_bookedByTooLong_throws() {
        givenRoomExists();

        assertThatThrownBy(() -> bookingService.createBooking(
                newBooking("x".repeat(101), null, "2030-02-01", "10:00", "11:00")))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Booked by must be at most 100 characters.");
    }

    @Test
    @DisplayName("createBooking rejects a purpose longer than 200 characters")
    void createBooking_purposeTooLong_throws() {
        givenRoomExists();

        assertThatThrownBy(() -> bookingService.createBooking(
                newBooking("Aydan", "x".repeat(201), "2030-02-01", "10:00", "11:00")))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Purpose must be at most 200 characters.");
    }

    // ---------- createBooking: date and time ----------

    @ParameterizedTest(name = "{0} {1}-{2} → \"{3}\"")
    @CsvSource(delimiter = '|', value = {
        "2030-02-30 | 10:00 | 11:00 | Date must be a valid date in YYYY-MM-DD format.",
        "2030-02-01 | 9:00  | 11:00 | Time must be in HH:mm format (24-hour).",
        "2030-02-01 | 10:00 | 24:00 | Time must be in HH:mm format (24-hour).",
        "2030-02-01 | 12:00 | 11:00 | Start time must be before end time.",
        "2030-02-01 | 07:00 | 09:00 | Bookings are allowed only between 08:00 and 22:00.",
        "2030-02-01 | 21:00 | 22:30 | Bookings are allowed only between 08:00 and 22:00."
    })
    @DisplayName("createBooking rejects invalid dates, times and ranges with the exact message")
    void createBooking_invalidDateOrTime_throws(String date, String start, String end, String message) {
        givenRoomExists();

        assertThatThrownBy(() -> bookingService.createBooking(newBooking("Aydan", null, date, start, end)))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage(message);
        verifyNoInteractions(bookingRepository);
    }

    // ---------- createBooking: not in the past (now = 2030-01-15 09:00) ----------

    @ParameterizedTest(name = "{0} at {1} is in the past")
    @CsvSource({"2030-01-15, 08:00, 10:00", "2030-01-14, 10:00, 11:00"})
    @DisplayName("createBooking rejects a booking that starts before now")
    void createBooking_inThePast_throws(String date, String start, String end) {
        givenRoomExists();

        assertThatThrownBy(() -> bookingService.createBooking(newBooking("Aydan", null, date, start, end)))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Cannot create a booking in the past.");
        verifyNoInteractions(bookingRepository);
    }

    @ParameterizedTest(name = "today at {0} is accepted")
    @ValueSource(strings = {"09:00", "10:00"})
    @DisplayName("createBooking accepts a booking today that starts now or later")
    void createBooking_todayNotInPast_isAccepted(String start) {
        givenRoomExists();
        LocalTime startTime = LocalTime.parse(start);
        when(bookingRepository.findOverlapping(ROOM_ID, LocalDate.of(2030, 1, 15), startTime, LocalTime.of(12, 0)))
                .thenReturn(List.of());
        givenSaveReturnsArgument();

        Booking saved = bookingService.createBooking(newBooking("Aydan", null, "2030-01-15", start, "12:00"));

        assertThat(saved.getStartTime()).isEqualTo(startTime);
    }

    // ---------- createBooking: no double booking ----------

    @Test
    @DisplayName("createBooking rejects an overlap and names the existing booking's times")
    void createBooking_overlap_throwsConflictWithExistingTimes() {
        givenRoomExists();
        Booking existing = booking("10:00", "11:30");
        when(bookingRepository.findOverlapping(ROOM_ID, FUTURE_DATE, LocalTime.of(11, 0), LocalTime.of(12, 0)))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> bookingService.createBooking(
                newBooking("Onur", null, "2030-02-01", "11:00", "12:00")))
                .isInstanceOf(BookingConflictException.class)
                .hasMessage("Room 'Lab 101' is already booked from 10:00 to 11:30 on 2030-02-01.");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking names the earliest booking when several overlap")
    void createBooking_severalOverlaps_namesEarliest() {
        givenRoomExists();
        when(bookingRepository.findOverlapping(ROOM_ID, FUTURE_DATE, LocalTime.of(9, 0), LocalTime.of(13, 0)))
                .thenReturn(List.of(booking("10:00", "11:00"), booking("12:00", "12:30")));

        assertThatThrownBy(() -> bookingService.createBooking(
                newBooking("Onur", null, "2030-02-01", "09:00", "13:00")))
                .isInstanceOf(BookingConflictException.class)
                .hasMessage("Room 'Lab 101' is already booked from 10:00 to 11:00 on 2030-02-01.");
    }

    @Test
    @DisplayName("createBooking accepts a back-to-back booking when no booking overlaps")
    void createBooking_touchingRange_isAccepted() {
        givenRoomExists();
        givenNoOverlap("11:30", "12:30");
        givenSaveReturnsArgument();

        Booking saved = bookingService.createBooking(newBooking("Onur", null, "2030-02-01", "11:30", "12:30"));

        assertThat(saved.getStartTime()).isEqualTo(LocalTime.of(11, 30));
        verify(bookingRepository).save(any(Booking.class));
    }

    // ---------- listBookings ----------

    @Test
    @DisplayName("listBookings without filters returns all bookings")
    void listBookings_noFilter_usesFindAll() {
        List<Booking> all = List.of(booking("10:00", "11:00"));
        when(bookingRepository.findAllByOrderByBookingDateAscStartTimeAscRoomIdAsc()).thenReturn(all);

        assertThat(bookingService.listBookings(null, null)).isEqualTo(all);
    }

    @Test
    @DisplayName("listBookings with a room id returns the bookings of that room")
    void listBookings_roomFilter_usesFindByRoomId() {
        givenRoomExists();
        List<Booking> bookings = List.of(booking("10:00", "11:00"));
        when(bookingRepository.findByRoomIdOrderByBookingDateAscStartTimeAsc(ROOM_ID)).thenReturn(bookings);

        assertThat(bookingService.listBookings(ROOM_ID, null)).isEqualTo(bookings);
    }

    @Test
    @DisplayName("listBookings with a date returns the bookings of that date")
    void listBookings_dateFilter_usesFindByBookingDate() {
        List<Booking> bookings = List.of(booking("10:00", "11:00"));
        when(bookingRepository.findByBookingDateOrderByStartTimeAscRoomIdAsc(FUTURE_DATE)).thenReturn(bookings);

        assertThat(bookingService.listBookings(null, "2030-02-01")).isEqualTo(bookings);
    }

    @Test
    @DisplayName("listBookings with a room id and a date uses both filters")
    void listBookings_roomAndDateFilter_usesBothFilters() {
        givenRoomExists();
        List<Booking> bookings = List.of(booking("10:00", "11:00"));
        when(bookingRepository.findByRoomIdAndBookingDateOrderByStartTimeAsc(ROOM_ID, FUTURE_DATE))
                .thenReturn(bookings);

        assertThat(bookingService.listBookings(ROOM_ID, "2030-02-01")).isEqualTo(bookings);
    }

    @Test
    @DisplayName("listBookings with an unknown room id throws RoomNotFoundException")
    void listBookings_unknownRoom_throws() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.listBookings(99L, null))
                .isInstanceOf(RoomNotFoundException.class)
                .hasMessage("Room with id 99 was not found.");
        verifyNoInteractions(bookingRepository);
    }

    @Test
    @DisplayName("listBookings with an invalid date throws InvalidInputException")
    void listBookings_invalidDate_throws() {
        assertThatThrownBy(() -> bookingService.listBookings(null, "2030-13-01"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Date must be a valid date in YYYY-MM-DD format.");
        verifyNoInteractions(bookingRepository);
    }

    // ---------- getBooking / cancelBooking ----------

    @Test
    @DisplayName("getBooking returns the booking when the id exists")
    void getBooking_existingId_returnsBooking() {
        Booking booking = booking("10:00", "11:00");
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        assertThat(bookingService.getBooking(7L)).isSameAs(booking);
    }

    @Test
    @DisplayName("getBooking throws BookingNotFoundException for an unknown id")
    void getBooking_unknownId_throws() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBooking(99L))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("Booking with id 99 was not found.");
    }

    @Test
    @DisplayName("cancelBooking deletes an existing booking")
    void cancelBooking_existingId_deletes() {
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking("10:00", "11:00")));

        bookingService.cancelBooking(7L);

        verify(bookingRepository).deleteById(7L);
    }

    @Test
    @DisplayName("cancelBooking throws BookingNotFoundException and deletes nothing for an unknown id")
    void cancelBooking_unknownId_throws() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(99L))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("Booking with id 99 was not found.");
        verify(bookingRepository, never()).deleteById(any());
    }

    // ---------- helpers ----------

    private void givenRoomExists() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
    }

    private void givenNoOverlap(String start, String end) {
        when(bookingRepository.findOverlapping(ROOM_ID, FUTURE_DATE, LocalTime.parse(start), LocalTime.parse(end)))
                .thenReturn(List.of());
    }

    private void givenSaveReturnsArgument() {
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static NewBooking newBooking(String bookedBy, String purpose, String date, String start, String end) {
        return new NewBooking(ROOM_ID, bookedBy, purpose, new TimeSlot(date, start, end));
    }

    private static Booking booking(String start, String end) {
        return new Booking(ROOM_ID, "Aydan", "", FUTURE_DATE, LocalTime.parse(start), LocalTime.parse(end));
    }
}
