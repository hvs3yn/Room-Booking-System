package com.roombookingsystem.roombookingsystem.exception;

import org.springframework.http.HttpStatus;

/** Thrown when user input breaks a validation rule (HTTP 400). */
public class InvalidInputException extends AppException {

    /** Creates the error with the exact message from the contract. */
    public InvalidInputException(String message) {
        super("INVALID_INPUT", HttpStatus.BAD_REQUEST, message);
    }
}
