package com.example.transaction.service;

import com.example.transaction.dto.BalanceEvent;
import com.example.transaction.kafka.BalanceEventProducer;
import com.example.transaction.dto.TransferRequest;
import com.example.transaction.dto.TransferResponse;
import com.example.transaction.entity.*;
import com.example.transaction.enums.ErrorCode;
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
    private final AccountLimitRepository limitRepository;
    private final IdempotencyKeyRepository idempotencyRepository;
    private final BalanceEventProducer producer;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountLimitRepository limitRepository, IdempotencyKeyRepository idempotencyRepository, BalanceEventProducer producer) {
        this.transactionRepository = transactionRepository;
        this.limitRepository = limitRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.producer = producer;
    }

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {

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
            producer.sendEvent(
                    BalanceEvent.builder()
                            .accountNumber(request.sourceAccount())
                            .amount(request.amount())
                            .transactionRef(txRef)
                            .type("DEBIT")
                            .timestamp(LocalDateTime.now())
                            .build()
            );

            transaction.setStatus(TransactionStatus.DEBIT_REQUESTED);
            transactionRepository.save(transaction);

        } catch (Exception ex) {

            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
        }

        return mapToResponse(transaction);
    }

    public TransferResponse getTransaction(String transactionRef) {
        Transaction tx =  transactionRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        return mapToResponse(tx);
    }

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

    private void resetIfNewDay(AccountLimit limit) {
        if (!limit.getLastReset().equals(LocalDate.now())) {
            limit.setUsedToday(BigDecimal.ZERO);
            limit.setLastReset(LocalDate.now());
        }
    }

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