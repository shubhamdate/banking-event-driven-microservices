package com.example.accounts.service;

import com.example.accounts.dto.AccountResponse;
import com.example.accounts.dto.AccountStatusResponse;
import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.enums.AccountStatus;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(
            String userId,
            CreateAccountRequest request
    );

    List<AccountResponse> getMyAccounts(String userId);

    AccountResponse getAccountByNumber(
            String userId,
            String accountNumber
    );

    AccountStatusResponse changeAccountStatus(
            String adminUserId,
            String accountNumber,
            AccountStatus newStatus
    );
}
