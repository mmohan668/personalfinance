package com.pf.common.exception.handler;

import com.pf.common.entity.generic.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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
