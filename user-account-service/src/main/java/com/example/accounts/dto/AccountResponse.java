package com.example.accounts.dto;

import com.example.accounts.enums.AccountStatus;
import com.example.accounts.enums.AccountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private String currency;
    private BigDecimal balance;
    private AccountStatus status;
}
