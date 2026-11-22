package com.roombooking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.roombooking.exception.DuplicateRoomNameException;
import com.roombooking.exception.InvalidInputException;
import com.roombooking.exception.RoomInUseException;
import com.roombooking.exception.RoomNotFoundException;
import com.roombooking.model.Room;
import com.roombooking.repository.BookingRepository;
import com.roombooking.repository.RoomRepository;
import com.roombooking.service.params.TimeSlot;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    private static final LocalDate DATE = LocalDate.of(2030, 1, 15);
    private static final LocalTime TEN = LocalTime.of(10, 0);
    private static final LocalTime ELEVEN = LocalTime.of(11, 0);
    private static final TimeSlot SLOT = new TimeSlot("2030-01-15", "10:00", "11:00");

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService(roomRepository, bookingRepository);
    }

    // ---------- createRoom ----------

    @Test
    @DisplayName("createRoom trims the name and saves the room")
    void createRoom_trimsNameAndSaves() {
        when(roomRepository.existsByNameIgnoreCase("Lab 101")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Room created = roomService.createRoom("  Lab 101  ", 30);

        assertThat(created.getName()).isEqualTo("Lab 101");
        assertThat(created.getCapacity()).isEqualTo(30);
    }

    @ParameterizedTest(name = "name \"{0}\" is rejected")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("createRoom rejects a null, empty or blank name")
    void createRoom_emptyName_throws(String name) {
        assertThatThrownBy(() -> roomService.createRoom(name, 30))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Room name must not be empty.");
        verifyNoInteractions(roomRepository);
    }

    @Test
    @DisplayName("createRoom rejects a name longer than 50 characters")
    void createRoom_nameTooLong_throws() {
        assertThatThrownBy(() -> roomService.createRoom("x".repeat(51), 30))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Room name must be at most 50 characters.");
    }

    @Test
    @DisplayName("createRoom accepts a name of exactly 50 characters")
    void createRoom_nameOfFiftyCharacters_isAccepted() {
        String name = "x".repeat(50);
        when(roomRepository.existsByNameIgnoreCase(name)).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(roomService.createRoom(name, 30).getName()).isEqualTo(name);
    }

    @ParameterizedTest(name = "capacity {0} is rejected")
    @NullSource
    @ValueSource(ints = {0, 501, -1})
    @DisplayName("createRoom rejects a capacity that is null or outside 1-500")
    void createRoom_invalidCapacity_throws(Integer capacity) {
        assertThatThrownBy(() -> roomService.createRoom("Lab 101", capacity))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Capacity must be a whole number between 1 and 500.");
        verifyNoInteractions(roomRepository);
    }

    @ParameterizedTest(name = "capacity {0} is accepted")
    @ValueSource(ints = {1, 500})
    @DisplayName("createRoom accepts the border capacities 1 and 500")
    void createRoom_borderCapacity_isAccepted(int capacity) {
        when(roomRepository.existsByNameIgnoreCase("Lab 101")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(roomService.createRoom("Lab 101", capacity).getCapacity()).isEqualTo(capacity);
    }

    @Test
    @DisplayName("createRoom rejects a duplicate name in a different case and does not save")
    void createRoom_duplicateNameIgnoringCase_throws() {
        when(roomRepository.existsByNameIgnoreCase("lab 101")).thenReturn(true);

        assertThatThrownBy(() -> roomService.createRoom(" lab 101 ", 30))
                .isInstanceOf(DuplicateRoomNameException.class)
                .hasMessage("A room named 'lab 101' already exists.");
        verify(roomRepository, never()).save(any());
    }

    // ---------- listRooms / getRoom ----------

    @Test
    @DisplayName("listRooms returns the rooms sorted by name from the repository")
    void listRooms_returnsSortedRooms() {
        List<Room> rooms = List.of(room(1L, "A-101", 10), room(2L, "B-202", 20));
        when(roomRepository.findAllByOrderByNameAsc()).thenReturn(rooms);

        assertThat(roomService.listRooms()).isEqualTo(rooms);
    }

    @Test
    @DisplayName("getRoom returns the room when the id exists")
    void getRoom_existingId_returnsRoom() {
        Room room = room(1L, "Lab 101", 30);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThat(roomService.getRoom(1L)).isSameAs(room);
    }

    @Test
    @DisplayName("getRoom throws RoomNotFoundException for an unknown id")
    void getRoom_unknownId_throws() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoom(99L))
                .isInstanceOf(RoomNotFoundException.class)
                .hasMessage("Room with id 99 was not found.");
    }

    // ---------- deleteRoom ----------

    @Test
    @DisplayName("deleteRoom deletes a room that has no bookings")
    void deleteRoom_withoutBookings_deletes() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, "Lab 101", 30)));
        when(bookingRepository.existsByRoomId(1L)).thenReturn(false);

        roomService.deleteRoom(1L);

        verify(roomRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteRoom refuses to delete a room that has bookings")
    void deleteRoom_withBookings_throws() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, "Lab 101", 30)));
        when(bookingRepository.existsByRoomId(1L)).thenReturn(true);

        assertThatThrownBy(() -> roomService.deleteRoom(1L))
                .isInstanceOf(RoomInUseException.class)
                .hasMessage("Room 'Lab 101' has bookings and cannot be deleted.");
        verify(roomRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteRoom throws RoomNotFoundException for an unknown id")
    void deleteRoom_unknownId_throws() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.deleteRoom(99L))
                .isInstanceOf(RoomNotFoundException.class)
                .hasMessage("Room with id 99 was not found.");
        verify(roomRepository, never()).deleteById(any());
    }

    // ---------- findFreeRooms ----------

    @Test
    @DisplayName("findFreeRooms leaves out rooms that are booked in the slot")
    void findFreeRooms_excludesBookedRooms() {
        Room roomA = room(1L, "A", 10);
        Room roomB = room(2L, "B", 40);
        when(bookingRepository.findBookedRoomIds(DATE, TEN, ELEVEN)).thenReturn(Set.of(1L));
        when(roomRepository.findAllByOrderByNameAsc()).thenReturn(List.of(roomA, roomB));

        assertThat(roomService.findFreeRooms(SLOT, null)).containsExactly(roomB);
    }

    @Test
    @DisplayName("findFreeRooms keeps only rooms with at least minCapacity seats")
    void findFreeRooms_filtersByMinCapacity() {
        Room roomA = room(1L, "A", 10);
        Room roomB = room(2L, "B", 40);
        when(bookingRepository.findBookedRoomIds(DATE, TEN, ELEVEN)).thenReturn(Set.of());
        when(roomRepository.findAllByOrderByNameAsc()).thenReturn(List.of(roomA, roomB));

        assertThat(roomService.findFreeRooms(SLOT, 20)).containsExactly(roomB);
    }

    @Test
    @DisplayName("findFreeRooms returns all free rooms when minCapacity is null")
    void findFreeRooms_withoutMinCapacity_returnsAllFreeRooms() {
        Room roomA = room(1L, "A", 10);
        Room roomB = room(2L, "B", 40);
        when(bookingRepository.findBookedRoomIds(DATE, TEN, ELEVEN)).thenReturn(Set.of());
        when(roomRepository.findAllByOrderByNameAsc()).thenReturn(List.of(roomA, roomB));

        assertThat(roomService.findFreeRooms(SLOT, null)).containsExactly(roomA, roomB);
    }

    @ParameterizedTest(name = "minCapacity {0} is rejected")
    @ValueSource(ints = {0, -5})
    @DisplayName("findFreeRooms rejects a minCapacity below 1")
    void findFreeRooms_invalidMinCapacity_throws(int minCapacity) {
        assertThatThrownBy(() -> roomService.findFreeRooms(SLOT, minCapacity))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Minimum capacity must be a positive whole number.");
        verifyNoInteractions(bookingRepository, roomRepository);
    }

    @Test
    @DisplayName("findFreeRooms rejects a slot whose start is after its end")
    void findFreeRooms_invalidTimeRange_throws() {
        TimeSlot reversed = new TimeSlot("2030-01-15", "11:00", "10:00");

        assertThatThrownBy(() -> roomService.findFreeRooms(reversed, null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Start time must be before end time.");
        verifyNoInteractions(bookingRepository, roomRepository);
    }

    @Test
    @DisplayName("findFreeRooms allows a slot in the past, because it is only a search")
    void findFreeRooms_pastSlot_isAllowed() {
        LocalDate pastDate = LocalDate.of(2000, 1, 1);
        when(bookingRepository.findBookedRoomIds(pastDate, TEN, ELEVEN)).thenReturn(Set.of());
        when(roomRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

        assertThat(roomService.findFreeRooms(new TimeSlot("2000-01-01", "10:00", "11:00"), null)).isEmpty();
    }

    private static Room room(Long id, String name, int capacity) {
        Room room = new Room(name, capacity);
        ReflectionTestUtils.setField(room, "id", id);
        return room;
    }
}
