package com.pf.common.exception.handler;

import com.pf.common.entity.generic.ApiResponse;
import com.pf.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(NoResourceFoundException ex, HttpServletRequest request) {
        log.warn(
                "Endpoint not found. Method: {}, URI: {}",
                request.getMethod(),
                request.getRequestURI()
        );
        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("The requested resource was not found. Please contact system administrator.")
                .build();
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.error(
                "ResourceNotFoundException occurred. Method: {}, URI: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex
        );
        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(
            Exception ex,
            HttpServletRequest request) {
        log.error(
                "Unhandled exception occurred. Method: {}, URI: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex
        );

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("An unexpected error occurred. Please contact system administrator.")
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
