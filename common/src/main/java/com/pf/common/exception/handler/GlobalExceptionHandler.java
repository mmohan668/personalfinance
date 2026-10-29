package com.pf.common.exception.handler;

import com.pf.common.dto.generic.ApiResponse;
import com.pf.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private boolean isActivationEndpoint(HttpServletRequest request) {
        return request.getRequestURI().endsWith("/users/activateAccount");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNotFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        log.warn(
                "Endpoint not found. Method: {}, URI: {}",
                request.getMethod(),
                request.getRequestURI()
        );

        if (isActivationEndpoint(request)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Activation endpoint was not found.");
        }

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("The requested resource was not found. Please contact system administrator.")
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.error(
                "ResourceNotFoundException occurred. Method: {}, URI: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex
        );

        if (isActivationEndpoint(request)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(ex.getMessage());
        }

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn(
                "Request validation failed. Method: {}, URI: {}, Message: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage()
        );

        if (isActivationEndpoint(request)) {
            return ResponseEntity
                    .badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(ex.getMessage());
        }

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(
            Exception ex,
            HttpServletRequest request) {

        log.error(
                "Unhandled exception occurred. Method: {}, URI: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex
        );

        if (isActivationEndpoint(request)) {
            Throwable rootCause = ex;

            while (rootCause.getCause() != null
                    && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }

            if (rootCause instanceof IllegalArgumentException
                    && rootCause.getMessage() != null) {
                return ResponseEntity
                        .badRequest()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(rootCause.getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Account activation failed. Please try again later.");
        }

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("An unexpected error occurred. Please contact system administrator.")
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}