package com.example.accounts.service.impl;

import com.example.accounts.dto.AccountResponse;
import com.example.accounts.dto.AccountStatusResponse;
import com.example.accounts.dto.CreateAccountRequest;
import com.example.accounts.entity.Account;
import com.example.accounts.entity.AccountAudit;
import com.example.accounts.entity.Customer;
import com.example.accounts.enums.AccountAction;
import com.example.accounts.enums.AccountStatus;
import com.example.accounts.exception.BusinessException;
import com.example.accounts.repository.AccountAuditRepository;
import com.example.accounts.repository.AccountRepository;
import com.example.accounts.repository.CustomerRepository;
import com.example.accounts.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AccountAuditRepository accountAuditRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    static final String CURRENCY = "INR";

    @Override
    @Transactional
    public AccountResponse createAccount(String userId, CreateAccountRequest request ) {

        Customer customer = customerRepository.findByUserId(userId)
                .orElseGet(() -> createCustomer(userId));

        boolean exists = accountRepository
                .findByCustomerIdAndAccountType(customer.getId(), request.getAccountType())
                .isPresent();

        if (exists) {
            throw new BusinessException(
                    "ACC_409",
                    "Account already exists for this type",
                    HttpStatus.CONFLICT
            );
        }

        Account account = new Account();
        account.setAccountNumber(accountNumberGenerator.generateAccountNumber());
        account.setCustomer(customer);
        account.setAccountType(request.getAccountType());
        account.setCurrency(CURRENCY);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);

        try {
            accountRepository.save(account);
        } catch (Exception ex) {
            throw new BusinessException(
                    "ACC_409",
                    "Account already exists for this type",
                    HttpStatus.CONFLICT
            );
        }

        saveAudit(account, AccountAction.CREATE, null, AccountStatus.ACTIVE, userId);

        return mapToResponse(account);
    }

    @Override
    public List<AccountResponse> getMyAccounts(String userId) throws BusinessException {

        try {
            Optional<Customer> customer = customerRepository.findByUserId(userId);

            if(customer.isEmpty()) {
                throw new BusinessException(
                        "ACC_404",
                        "Customer not found",
                        HttpStatus.NOT_FOUND
                );
            }

            return accountRepository.findByCustomerId(customer.get().getId())
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        } catch (Exception e) {
            throw new BusinessException(
                    "ACC_405",
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        }
    }

    @Override
    public AccountResponse getAccountByNumber(String userId, String accountNumber) {

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(
                        "ACC_404",
                        "Customer not found",
                        HttpStatus.NOT_FOUND
                ));

        Account account = accountRepository
                .findByAccountNumberAndCustomer(accountNumber, customer)
                .orElseThrow(() -> new BusinessException(
                "ACC_404",
                "Account not found",
                HttpStatus.NOT_FOUND
        ));


        return mapToResponse(account);
    }

    @Override
    @Transactional
    public AccountStatusResponse changeAccountStatus(
            String adminUserId,
            String accountNumber,
            AccountStatus newStatus
    ) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new BusinessException(
                                "ACC_404",
                                "Account not found",
                                HttpStatus.NOT_FOUND
                        )
                );
        validateStatusTransition(account.getStatus(), newStatus);

        AccountStatus oldStatus = account.getStatus();

        account.setStatus(newStatus);
        accountRepository.save(account);

        saveAudit(
                account,
                AccountAction.STATUS_CHANGE,
                oldStatus,
                newStatus,
                adminUserId
        );

        return AccountStatusResponse.builder()
                .accountNumber(accountNumber)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(adminUserId)
                .build();

    }

    // ---------- Helpers ----------

    private Customer createCustomer(String userId) {

        Customer customer = new Customer();
        customer.setUserId(userId);
        customer.setStatus(AccountStatus.ACTIVE);

        customerRepository.save(customer);
        return customer;
    }

    private void saveAudit(
            Account account,
            AccountAction action,
            AccountStatus oldStatus,
            AccountStatus newStatus,
            String performedBy
    ) {
        AccountAudit audit = new AccountAudit();
        audit.setAccount(account);
        audit.setAction(action);
        audit.setOldStatus(oldStatus);
        audit.setNewStatus(newStatus);
        audit.setPerformedBy(performedBy);
        accountAuditRepository.save(audit);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .status(account.getStatus())
                .id(account.getId())
                .build();
    }

    private void validateStatusTransition(
            AccountStatus current,
            AccountStatus next
    ) {
        if (current == AccountStatus.CLOSED) {
            throw new BusinessException(
                    "ACC_400",
                    "Closed account cannot be modified",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (current == next) {
            throw new BusinessException(
                    "ACC_400",
                    "Account already in status " + current,
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}