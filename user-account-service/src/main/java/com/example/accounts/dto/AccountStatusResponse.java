package com.example.accounts.dto;

import com.example.accounts.enums.AccountStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountStatusResponse {
    private String accountNumber;
    private AccountStatus oldStatus;
    private AccountStatus newStatus;
    private String changedBy;
}
