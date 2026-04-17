package com.example.accounts.service.impl;

import com.example.accounts.dto.BalanceOperationRequest;
import com.example.accounts.entity.Account;
import com.example.accounts.enums.AccountStatus;
import com.example.accounts.exception.BusinessException;
import com.example.accounts.repository.AccountRepository;
import com.example.accounts.service.AccountBalanceService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AccountBalanceServiceImpl implements AccountBalanceService {

    private final AccountRepository accountRepository;

    public AccountBalanceServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    @Override
    public void debit(BalanceOperationRequest request) {

        validateAmount(request.getAmount());

        Account account = accountRepository
                .findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new BusinessException(
                        "ACC_404",
                        "Account not found",
                        HttpStatus.NOT_FOUND
                ));

        validateAccountState(account);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException(
                    "BAL_001",
                    "Insufficient balance",
                    HttpStatus.BAD_REQUEST
            );
        }

        BigDecimal newBalance = account.getBalance()
                .subtract(request.getAmount())
                .setScale(4, RoundingMode.HALF_UP);

        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    @Transactional
    @Override
    public void credit(BalanceOperationRequest request) {

        validateAmount(request.getAmount());

        Account account = accountRepository
                .findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new BusinessException(
                        "ACC_404",
                        "Account not found",
                        HttpStatus.NOT_FOUND
                ));

        validateAccountState(account);

        BigDecimal newBalance = account.getBalance()
                .add(request.getAmount())
                .setScale(4, RoundingMode.HALF_UP);

        if (newBalance.compareTo(new BigDecimal("9999999999999.9999")) > 0) {
            throw new BusinessException(
                    "BAL_999",
                    "Balance overflow",
                    HttpStatus.BAD_REQUEST
            );
        }

        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "VAL_400",
                    "Invalid transaction amount",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateAccountState(Account account) {
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException(
                    "ACC_403",
                    "Account is closed",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BusinessException(
                    "ACC_403",
                    "Account is blocked",
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
