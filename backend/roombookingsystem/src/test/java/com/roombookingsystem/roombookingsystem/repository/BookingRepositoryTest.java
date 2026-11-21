package com.roombookingsystem.roombookingsystem.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.roombookingsystem.roombookingsystem.model.Booking;
import com.roombookingsystem.roombookingsystem.model.Room;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

/**
 * Tests BookingRepository against an in-memory H2 database created from schema.sql.
 * Every test starts with room A booked on 2030-12-01 from 10:00 to 11:30.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryTest {

    private static final LocalDate DAY = LocalDate.of(2030, 12, 1);
    private static final LocalDate OTHER_DAY = LocalDate.of(2030, 12, 2);

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Room roomA;
    private Room roomB;
    private Booking existing;

    @BeforeEach
    void setUp() {
        roomA = roomRepository.save(new Room("A-101", 30));
        roomB = roomRepository.save(new Room("B-201", 60));
        existing = bookingRepository.save(book(roomA, DAY, "10:00", "11:30"));
    }

    @Test
    @DisplayName("save gives the booking an id and keeps all fields")
    void save_assignsId_andKeepsAllFields() {
        Booking found = bookingRepository.findById(existing.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getRoomId()).isEqualTo(roomA.getId());
        assertThat(found.getBookedBy()).isEqualTo("Onur");
        assertThat(found.getPurpose()).isEqualTo("Study group");
        assertThat(found.getBookingDate()).isEqualTo(DAY);
        assertThat(found.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(found.getEndTime()).isEqualTo(LocalTime.of(11, 30));
    }

    @Test
    @DisplayName("findOverlapping finds a booking that partly overlaps (11:00-12:00)")
    void findOverlapping_detectsPartialOverlap() {
        assertThat(overlapping(roomA, DAY, "11:00", "12:00"))
                .extracting(Booking::getId)
                .containsExactly(existing.getId());
    }

    @Test
    @DisplayName("findOverlapping finds bookings inside (10:30-11:00) and around (09:00-13:00)")
    void findOverlapping_detectsBookingInsideAndAround() {
        assertThat(overlapping(roomA, DAY, "10:30", "11:00")).hasSize(1);
        assertThat(overlapping(roomA, DAY, "09:00", "13:00")).hasSize(1);
    }

    @Test
    @DisplayName("findOverlapping ignores ranges that only touch (11:30-12:30, 09:00-10:00)")
    void findOverlapping_ignoresTouchingRanges() {
        assertThat(overlapping(roomA, DAY, "11:30", "12:30")).isEmpty();
        assertThat(overlapping(roomA, DAY, "09:00", "10:00")).isEmpty();
    }

    @Test
    @DisplayName("findOverlapping ignores other rooms and other dates")
    void findOverlapping_ignoresOtherRoomsAndDates() {
        assertThat(overlapping(roomB, DAY, "10:00", "11:30")).isEmpty();
        assertThat(overlapping(roomA, OTHER_DAY, "10:00", "11:30")).isEmpty();
    }

    @Test
    @DisplayName("findBookedRoomIds returns only rooms that are busy in the slot")
    void findBookedRoomIds_returnsOnlyBusyRooms() {
        assertThat(bookingRepository.findBookedRoomIds(DAY, LocalTime.of(10, 30), LocalTime.of(11, 0)))
                .containsExactly(roomA.getId());
        assertThat(bookingRepository.findBookedRoomIds(DAY, LocalTime.of(11, 30), LocalTime.of(12, 0)))
                .isEmpty();
    }

    @Test
    @DisplayName("the four list methods filter by room and date and sort by date and time")
    void filterMethods_filterByRoomAndDate_andSort() {
        Booking early = bookingRepository.save(book(roomA, DAY, "08:00", "09:00"));
        Booking otherRoom = bookingRepository.save(book(roomB, DAY, "09:00", "10:00"));
        Booking nextDay = bookingRepository.save(book(roomA, OTHER_DAY, "08:00", "09:00"));

        assertThat(ids(bookingRepository.findAllByOrderByBookingDateAscStartTimeAscRoomIdAsc()))
                .containsExactly(early.getId(), otherRoom.getId(), existing.getId(), nextDay.getId());
        assertThat(ids(bookingRepository.findByRoomIdOrderByBookingDateAscStartTimeAsc(roomA.getId())))
                .containsExactly(early.getId(), existing.getId(), nextDay.getId());
        assertThat(ids(bookingRepository.findByBookingDateOrderByStartTimeAscRoomIdAsc(DAY)))
                .containsExactly(early.getId(), otherRoom.getId(), existing.getId());
        assertThat(ids(bookingRepository.findByRoomIdAndBookingDateOrderByStartTimeAsc(roomA.getId(), DAY)))
                .containsExactly(early.getId(), existing.getId());
    }

    @Test
    @DisplayName("existsByRoomId is true only for rooms with bookings")
    void existsByRoomId_trueOnlyWhenRoomHasBookings() {
        assertThat(bookingRepository.existsByRoomId(roomA.getId())).isTrue();
        assertThat(bookingRepository.existsByRoomId(roomB.getId())).isFalse();
    }

    private Booking book(Room room, LocalDate date, String start, String end) {
        return new Booking(room.getId(), "Onur", "Study group", date,
                LocalTime.parse(start), LocalTime.parse(end));
    }

    private List<Booking> overlapping(Room room, LocalDate date, String start, String end) {
        return bookingRepository.findOverlapping(room.getId(), date,
                LocalTime.parse(start), LocalTime.parse(end));
    }

    private static List<Long> ids(List<Booking> bookings) {
        return bookings.stream().map(Booking::getId).toList();
    }
}
