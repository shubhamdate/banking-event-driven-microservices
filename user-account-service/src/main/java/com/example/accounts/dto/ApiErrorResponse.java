package com.example.accounts.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiErrorResponse {
    private boolean success;
    private ApiError error;
}
