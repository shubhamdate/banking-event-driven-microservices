package com.example.transaction.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BalanceResultEvent {

    private String transactionRef;
    private String accountNumber;
    private String type;
    private String status;
    private String errorMessage;
    private LocalDateTime timestamp;
}