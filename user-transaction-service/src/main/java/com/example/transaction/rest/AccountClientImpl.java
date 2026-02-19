package com.example.transaction.rest;

import com.example.transaction.dto.BalanceOperationRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountClientImpl implements AccountClient {

    private final RestClient restClient;

    @Value("${services.account.url}")
    private String accountServiceUrl;

    @Override
    @CircuitBreaker(name = "accountService", fallbackMethod = "debitFallback")
    public void debit(String token,
                      String accountNumber,
                      BigDecimal amount,
                      String transactionRef) {

        log.info("Calling Account debit API for account: {}", accountNumber);

        restClient.post()
                .uri(accountServiceUrl + "/internal/balance/debit")
                .header("Authorization", token)
                .body(new BalanceOperationRequest(accountNumber, amount, transactionRef))
                .retrieve()
                .toBodilessEntity();

        log.info("Debit successful for transactionRef: {}", transactionRef);
    }

    @Override
    @CircuitBreaker(name = "accountService", fallbackMethod = "creditFallback")
    public void credit(String token,
                       String accountNumber,
                       BigDecimal amount,
                       String transactionRef) {

        log.info("Calling Account credit API for account: {}", accountNumber);

        restClient.post()
                .uri(accountServiceUrl + "/internal/balance/credit")
                .header("Authorization", token)
                .body(new BalanceOperationRequest(accountNumber, amount, transactionRef))
                .retrieve()
                .toBodilessEntity();

        log.info("Credit successful for transactionRef: {}", transactionRef);
    }

    // Fallback
    public void debitFallback(String token,
                              String accountNumber,
                              BigDecimal amount,
                              String transactionRef,
                              Throwable ex) {

        log.error("Debit fallback triggered. Account service unavailable", ex);
        throw new RuntimeException("Account service unavailable during debit");
    }

    public void creditFallback(String token,
                               String accountNumber,
                               BigDecimal amount,
                               String transactionRef,
                               Throwable ex) {

        log.error("Credit fallback triggered. Account service unavailable", ex);
        throw new RuntimeException("Account service unavailable during credit");
    }
}