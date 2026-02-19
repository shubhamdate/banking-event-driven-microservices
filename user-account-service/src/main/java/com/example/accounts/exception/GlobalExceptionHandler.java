package com.example.accounts.exception;

import com.example.accounts.dto.ApiError;
import com.example.accounts.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Business errors (expected)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        return buildError(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getStatus(),
                request.getRequestURI()
        );
    }

    // Security errors
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildError(
                "SEC_403",
                "Access denied",
                HttpStatus.FORBIDDEN,
                request.getRequestURI()
        );
    }

    // Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .orElse("Validation failed");

        return buildError(
                "VAL_400",
                message,
                HttpStatus.BAD_REQUEST,
                request.getRequestURI()
        );
    }

    // Fallback (unexpected errors)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildError(
                "GEN_500",
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI()
        );
    }

    private ResponseEntity<ApiErrorResponse> buildError(
            String code,
            String message,
            HttpStatus status,
            String path
    ) {
        return ResponseEntity.status(status)
                .body(
                        ApiErrorResponse.builder()
                                .success(false)
                                .error(
                                        ApiError.builder()
                                                .code(code)
                                                .message(message)
                                                .path(path)
                                                .timestamp(LocalDateTime.now())
                                                .build()
                                )
                                .build()
                );
    }
}
