package com.example.employee.exception;

/**
 * Thrown when a business rule blocks an operation, for example deleting a department that still has employees.
 */
public class OperationNotAllowedException extends RuntimeException {

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
