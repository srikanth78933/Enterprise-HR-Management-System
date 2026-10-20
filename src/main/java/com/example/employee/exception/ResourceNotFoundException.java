package com.example.employee.exception;

/**
 * Thrown when an employee or department with the requested ID does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
