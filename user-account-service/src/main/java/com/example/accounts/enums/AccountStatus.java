package com.example.accounts.enums;

import com.example.accounts.exception.BusinessException;
import org.springframework.http.HttpStatus;

public enum AccountStatus {
    ACTIVE,
    BLOCKED,
    CLOSED;

    public static AccountStatus from(String value) {
        try {
            return AccountStatus.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new BusinessException(
                    "ACC_400",
                    "Invalid account status",
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
