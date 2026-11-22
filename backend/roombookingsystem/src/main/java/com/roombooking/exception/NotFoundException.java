package com.roombooking.exception;

import org.springframework.http.HttpStatus;

/** Base class for "the requested thing does not exist" errors (HTTP 404). */
public abstract class NotFoundException extends AppException {

    /** Creates a not-found error with its specific code and message. */
    protected NotFoundException(String code, String message) {
        super(code, HttpStatus.NOT_FOUND, message);
    }
}
