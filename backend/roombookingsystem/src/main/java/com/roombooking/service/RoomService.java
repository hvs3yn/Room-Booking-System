package com.roombooking.service;

import com.roombooking.exception.DuplicateRoomNameException;
import com.roombooking.exception.InvalidInputException;
import com.roombooking.exception.RoomInUseException;
import com.roombooking.exception.RoomNotFoundException;
import com.roombooking.model.Room;
import com.roombooking.repository.BookingRepository;
import com.roombooking.repository.RoomRepository;
import com.roombooking.service.params.ParsedSlot;
import com.roombooking.service.params.TimeSlot;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business rules for rooms: creating, listing, deleting and finding free rooms. */
@Service
public class RoomService {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MIN_CAPACITY = 1;
    private static final int MAX_CAPACITY = 500;
    private static final String EMPTY_NAME = "Room name must not be empty.";
    private static final String NAME_TOO_LONG = "Room name must be at most 50 characters.";
    private static final String INVALID_CAPACITY = "Capacity must be a whole number between 1 and 500.";
    private static final String INVALID_MIN_CAPACITY = "Minimum capacity must be a positive whole number.";

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    /** Creates the service with the repositories it needs (constructor injection). */
    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Creates a new room after checking its name and capacity.
     *
     * @param name     the room name; it is trimmed and must be 1–50 characters (BR-1)
     * @param capacity the number of seats, from 1 to 500 (BR-3)
     * @return the saved room with its new id
     * @throws InvalidInputException      if the name or capacity is invalid
     * @throws DuplicateRoomNameException if a room with the same name exists, ignoring case (BR-2)
     */
    @Transactional
    public Room createRoom(String name, Integer capacity) {
        validateName(name);
        validateCapacity(capacity);
        String trimmedName = name.strip();
        if (roomRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateRoomNameException(trimmedName);
        }
        return roomRepository.save(new Room(trimmedName, capacity));
    }

    /**
     * Returns all rooms sorted by name.
     *
     * @return the rooms, or an empty list if there are none
     */
    @Transactional(readOnly = true)
    public List<Room> listRooms() {
        return roomRepository.findAllByOrderByNameAsc();
    }

    /**
     * Returns one room by its id.
     *
     * @param roomId the id of the room
     * @return the room
     * @throws RoomNotFoundException if no room has this id
     */
    @Transactional(readOnly = true)
    public Room getRoom(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
    }

    /**
     * Deletes a room that has no bookings.
     *
     * @param roomId the id of the room to delete
     * @throws RoomNotFoundException if no room has this id
     * @throws RoomInUseException    if the room still has bookings (BR-14)
     */
    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = getRoom(roomId);
        if (bookingRepository.existsByRoomId(roomId)) {
            throw new RoomInUseException(room.getName());
        }
        roomRepository.deleteById(roomId);
    }

    /**
     * Finds all rooms that have no booking overlapping the given slot (BR-12, BR-13).
     * A slot in the past is allowed here, because this is only a search (no BR-8).
     *
     * @param slot        the date ("YYYY-MM-DD") and the start and end times ("HH:mm")
     * @param minCapacity the minimum number of seats, or {@code null} for no filter
     * @return the free rooms sorted by name, or an empty list
     * @throws InvalidInputException if the date, times or minimum capacity are invalid
     */
    @Transactional(readOnly = true)
    public List<Room> findFreeRooms(TimeSlot slot, Integer minCapacity) {
        ParsedSlot parsed = TimeRules.parseSlot(slot);
        validateMinCapacity(minCapacity);

        Set<Long> bookedRoomIds = bookingRepository.findBookedRoomIds(parsed.date(), parsed.start(), parsed.end());
        return roomRepository.findAllByOrderByNameAsc().stream()
                .filter(room -> !bookedRoomIds.contains(room.getId()))
                .filter(room -> hasEnoughSeats(room, minCapacity))
                .toList();
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException(EMPTY_NAME);
        }
        if (name.strip().length() > MAX_NAME_LENGTH) {
            throw new InvalidInputException(NAME_TOO_LONG);
        }
    }

    private void validateCapacity(Integer capacity) {
        if (capacity == null || capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new InvalidInputException(INVALID_CAPACITY);
        }
    }

    private void validateMinCapacity(Integer minCapacity) {
        if (minCapacity != null && minCapacity < MIN_CAPACITY) {
            throw new InvalidInputException(INVALID_MIN_CAPACITY);
        }
    }

    private boolean hasEnoughSeats(Room room, Integer minCapacity) {
        return minCapacity == null || room.getCapacity() >= minCapacity;
    }
}
