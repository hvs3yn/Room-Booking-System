package com.roombookingsystem.roombookingsystem.exception;

import org.springframework.http.HttpStatus;

/** Base class for errors where the request conflicts with existing data (HTTP 409). */
public abstract class ConflictException extends AppException {

    /** Creates a conflict error with its specific code and message. */
    protected ConflictException(String code, String message) {
        super(code, HttpStatus.CONFLICT, message);
    }
}
