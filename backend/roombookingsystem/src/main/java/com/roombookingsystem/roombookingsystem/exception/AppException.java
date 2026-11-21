package com.roombookingsystem.roombookingsystem.exception;

import org.springframework.http.HttpStatus;

/** Base class for all expected business errors; the API turns it into a JSON error response. */
public abstract class AppException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    /** Creates an error with a machine-readable code, an HTTP status and a user-facing message. */
    protected AppException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    /** Returns the machine-readable error code, for example {@code BOOKING_CONFLICT}. */
    public String getCode() {
        return code;
    }

    /** Returns the HTTP status the API should answer with. */
    public HttpStatus getStatus() {
        return status;
    }
}
