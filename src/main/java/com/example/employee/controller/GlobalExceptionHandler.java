package com.example.employee.controller;

import com.example.employee.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Shows a friendly error page instead of a stack trace.
 * Any other unexpected error is rendered by Spring Boot using templates/error.html.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return errorPage(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleBadRequest(MethodArgumentTypeMismatchException ex) {
        log.warn("Invalid request parameter '{}': {}", ex.getName(), ex.getValue());
        return errorPage(HttpStatus.BAD_REQUEST, "The request contained an invalid value.");
    }

    private ModelAndView errorPage(HttpStatus status, String message) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.setStatus(status);
        modelAndView.addObject("status", status.value());
        modelAndView.addObject("error", status.getReasonPhrase());
        modelAndView.addObject("message", message);
        return modelAndView;
    }
}
