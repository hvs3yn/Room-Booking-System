package com.roombooking.exception;

/** Thrown when a room with the same name (ignoring case) already exists. */
public class DuplicateRoomNameException extends ConflictException {

    /** Creates the error for the name the user typed (already trimmed). */
    public DuplicateRoomNameException(String name) {
        super("DUPLICATE_ROOM_NAME", "A room named '" + name + "' already exists.");
    }
}
