package com.example.transaction.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BalanceEvent {

    private String accountNumber;
    private BigDecimal amount;
    private String transactionRef;
    private String type; // DEBIT / CREDIT
    private LocalDateTime timestamp;
}