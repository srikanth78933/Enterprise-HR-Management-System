package com.example.employee.exception;

/**
 * Thrown when a value that must be unique (employee ID, email, department name) already exists.
 * The field name lets the controller show the error next to the right form input.
 */
public class DuplicateValueException extends RuntimeException {

    private final String field;

    public DuplicateValueException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
