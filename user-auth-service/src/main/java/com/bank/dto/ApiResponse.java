package com.bank.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ApiResponse {

    private boolean success;
    private String message;
    private String error;
    private Object data;

    public ApiResponse(boolean success, String message, List<?> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean success, String message, Map<?, ?> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public static ApiResponse success(List<?> data, String message) {
        return new ApiResponse(true, message, data);
    }

    public static ApiResponse success(Map<?, ?> data, String message) {
        return new ApiResponse(true, message, data);
    }

    public static ApiResponse error(String error) {
        return new ApiResponse(false, error);
    }
}
