package com.example.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BalanceOperationRequest{
    @NotBlank
    private String accountNumber;
    @NotNull
    private BigDecimal amount;
    @NotBlank
    private String transactionRef;
}