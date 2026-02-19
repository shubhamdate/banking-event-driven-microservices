package com.example.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(

        @NotBlank
        String sourceAccount,

        @NotBlank
        String destinationAccount,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotBlank
        String currency,

        @NotBlank
        String idempotencyKey
) {}