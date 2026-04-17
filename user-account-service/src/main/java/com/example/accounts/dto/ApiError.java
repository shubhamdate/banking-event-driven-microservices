package com.example.accounts.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiError {
    private String code;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}
