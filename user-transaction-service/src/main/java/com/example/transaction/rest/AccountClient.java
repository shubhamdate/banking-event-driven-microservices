package com.example.transaction.rest;

import java.math.BigDecimal;

public interface AccountClient {

    void debit(String token, String accountNumber, BigDecimal amount, String transactionRef);

    void credit(String token, String accountNumber, BigDecimal amount, String transactionRef);
}