package com.example.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BalanceOperationRequest(

        @NotBlank
        String accountNumber,

        @NotNull
        BigDecimal amount,

        @NotBlank
        String transactionRef
) {}
