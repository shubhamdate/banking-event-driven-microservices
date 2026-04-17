package com.example.transaction.dto;

import com.example.transaction.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(

        String transactionRef,
        TransactionStatus status,
        BigDecimal amount,
        String currency,
        LocalDateTime createdAt
) {}