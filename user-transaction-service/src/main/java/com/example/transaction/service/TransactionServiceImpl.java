package com.example.transaction.service;

import com.example.transaction.rest.AccountClient;
import com.example.transaction.dto.TransferRequest;
import com.example.transaction.dto.TransferResponse;
import com.example.transaction.entity.*;
import com.example.transaction.enums.ErrorCode;
import com.example.transaction.enums.LedgerEntryType;
import com.example.transaction.enums.TransactionStatus;
import com.example.transaction.enums.TransactionType;
import com.example.transaction.exception.BusinessException;
import com.example.transaction.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerRepository;
    private final AccountLimitRepository limitRepository;
    private final IdempotencyKeyRepository idempotencyRepository;
    private final AccountClient accountClient;

    public TransactionServiceImpl(TransactionRepository transactionRepository, LedgerEntryRepository ledgerRepository, AccountLimitRepository limitRepository, IdempotencyKeyRepository idempotencyRepository, AccountClient accountClient) {
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
        this.limitRepository = limitRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.accountClient = accountClient;
    }

    @Override
    @Transactional
    public TransferResponse transfer(String token, TransferRequest request) {

        String txRef = UUID.randomUUID().toString();

        // Reserve idempotency first
        try {
            reserveIdempotency(request.idempotencyKey(), txRef);
        } catch (BusinessException ex) {
            // Duplicate → return existing transaction
            Transaction existing = fetchExistingTransaction(request.idempotencyKey());
            return mapToResponse(existing);
        }

        // Enforce limits
        enforceLimits(request.sourceAccount(), request.amount());

        // Create PENDING transaction
        Transaction transaction = createPendingTransaction(request, txRef);

        try {

            // Debit
            accountClient.debit(token, request.sourceAccount(), request.amount(), txRef);

            // Credit
            accountClient.credit(token, request.destinationAccount(), request.amount(), txRef);

            // Ledger
            createLedgerEntries(transaction);

            // Mark success
            transaction.setStatus(TransactionStatus.SUCCESS);

        } catch (Exception ex) {

            // Compensation
            compensate(token, transaction, request);

            transaction.setStatus(TransactionStatus.FAILED);
        }

        return mapToResponse(transaction);
    }

    // ===================== LIMIT LOGIC =====================

    private void enforceLimits(String accountNumber, BigDecimal amount) {

        AccountLimit limit = limitRepository
                .findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new RuntimeException("Limit not configured"));

        resetIfNewDay(limit);

        if (amount.compareTo(limit.getPerTxLimit()) > 0) {
            throw new BusinessException(
                    ErrorCode.LIMIT_EXCEEDED,
                    "Per transaction limit exceeded"
            );
        }

        if (limit.getUsedToday().add(amount)
                .compareTo(limit.getDailyLimit()) > 0) {
            throw new BusinessException(
                    ErrorCode.DAILY_LIMIT_EXCEEDED,
                    "Daily transaction limit exceeded"
            );
        }

        limit.setUsedToday(limit.getUsedToday().add(amount));
    }

    // ================= Idempotency Check ================
    private void reserveIdempotency(String key, String txRef) {
        try {
            idempotencyRepository.save(
                    IdempotencyKey.builder()
                            .id(key)
                            .transactionRef(txRef)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_REQUEST,
                    "Duplicate transfer request"
            );
        }
    }

    private Transaction createPendingTransaction(TransferRequest request, String txRef) {

        Transaction transaction = Transaction.builder()
                .transactionRef(txRef)
                .sourceAccount(request.sourceAccount())
                .destinationAccount(request.destinationAccount())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .amount(request.amount())
                .currency(request.currency())
                .initiatedBy("USER")
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }

    private Transaction fetchExistingTransaction(String key) {
        return idempotencyRepository.findById(key)
                .flatMap(record ->
                        transactionRepository.findByTransactionRef(
                                record.getTransactionRef()))
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INTERNAL_ERROR,
                                "Idempotency record corrupted"));
    }

    //=================== Reset Date ===================

    private void resetIfNewDay(AccountLimit limit) {
        if (!limit.getLastReset().equals(LocalDate.now())) {
            limit.setUsedToday(BigDecimal.ZERO);
            limit.setLastReset(LocalDate.now());
        }
    }

    // ===================== LEDGER =====================

    private void createLedgerEntries(Transaction tx) {

        ledgerRepository.save(
                LedgerEntry.builder()
                        .transactionId(tx.getId())
                        .accountNumber(tx.getSourceAccount())
                        .entryType(LedgerEntryType.DEBIT)
                        .amount(tx.getAmount())
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        ledgerRepository.save(
                LedgerEntry.builder()
                        .transactionId(tx.getId())
                        .accountNumber(tx.getDestinationAccount())
                        .entryType(LedgerEntryType.CREDIT)
                        .amount(tx.getAmount())
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    // ===================== COMPENSATION =====================

    private void compensate(String token, Transaction tx, TransferRequest request) {

        try {
            accountClient.credit(
                    token,
                    request.sourceAccount(),
                    request.amount(),
                    tx.getTransactionRef()
            );
        } catch (Exception ignored) {
            // In real banking → send to dead letter queue / retry scheduler
        }
    }

    // ===================== MAPPER =====================

    private TransferResponse mapToResponse(Transaction tx) {
        return new TransferResponse(
                tx.getTransactionRef(),
                tx.getStatus(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getCreatedAt()
        );
    }
}