package com.example.accounts.kafka.event;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BalanceResultEvent {

    private String transactionRef;
    private String accountNumber;
    private String type; // DEBIT / CREDIT
    private String status; // SUCCESS / FAILED
    private String errorMessage;
    private LocalDateTime timestamp;
}