package com.example.accounts.service;

import com.example.accounts.dto.BalanceOperationRequest;

public interface AccountBalanceService {

    void debit(BalanceOperationRequest request);

    void credit(BalanceOperationRequest request);
}
