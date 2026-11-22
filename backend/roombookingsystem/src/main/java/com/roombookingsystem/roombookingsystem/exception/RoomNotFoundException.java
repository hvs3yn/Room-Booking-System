package com.roombookingsystem.roombookingsystem.exception;

/** Thrown when no room exists with the given id. */
public class RoomNotFoundException extends NotFoundException {

    /** Creates the error for the missing room id. */
    public RoomNotFoundException(Long roomId) {
        super("ROOM_NOT_FOUND", "Room with id " + roomId + " was not found.");
    }
}
