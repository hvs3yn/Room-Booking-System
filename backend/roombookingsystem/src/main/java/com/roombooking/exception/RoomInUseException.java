package com.roombooking.exception;

/** Thrown when someone tries to delete a room that still has bookings. */
public class RoomInUseException extends ConflictException {

    /** Creates the error for the room with the given name. */
    public RoomInUseException(String roomName) {
        super("ROOM_IN_USE", "Room '" + roomName + "' has bookings and cannot be deleted.");
    }
}
