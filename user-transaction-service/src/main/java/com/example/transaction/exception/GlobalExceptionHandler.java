package com.example.transaction.exception;

import com.example.transaction.dto.ErrorResponse;
import com.example.transaction.enums.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {

        ErrorResponse response = new ErrorResponse(
                ex.getErrorCode().name(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {

        ex.printStackTrace(); // 🔥 IMPORTANT for debugging

        ErrorResponse response = new ErrorResponse(
                ErrorCode.INTERNAL_ERROR.name(),
                ex.getMessage(), // show real message
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
