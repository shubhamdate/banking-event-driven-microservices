package com.example.transaction.kafka;

import com.example.transaction.dto.BalanceEvent;
import com.example.transaction.dto.BalanceResultEvent;
import com.example.transaction.entity.LedgerEntry;
import com.example.transaction.enums.LedgerEntryType;
import com.example.transaction.enums.TransactionStatus;
import com.example.transaction.repository.LedgerEntryRepository;
import com.example.transaction.repository.TransactionRepository;
import com.example.transaction.entity.Transaction;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Transactional
@RequiredArgsConstructor
public class TransactionResultConsumer {

    private final TransactionRepository repository;

    private final LedgerEntryRepository ledgerRepository;

    private final BalanceEventProducer producer;

    private static final Logger log =  LoggerFactory.getLogger(TransactionResultConsumer.class);

    @KafkaListener(topics = "account-balance-response", groupId = "transaction-group")
    public void handle(BalanceResultEvent event) {

        log.info("TRANSACTION RECEIVED RESULT {}", event);

        Transaction tx = repository.findByTransactionRef(event.getTransactionRef())
                .orElseThrow();

        // Ignore if already terminal
        if (tx.getStatus() == TransactionStatus.SUCCESS ||
                tx.getStatus() == TransactionStatus.FAILED) {
            return;
        }

        // HANDLE DEBIT RESPONSE
        if ("DEBIT".equals(event.getType())) {

            if ("FAILED".equals(event.getStatus())) {
                tx.setStatus(TransactionStatus.FAILED);
                repository.save(tx);
                return;
            }

            // Debit success
            tx.setDebitCompleted(true);
            tx.setStatus(TransactionStatus.DEBITED);

            sendCreditEvent(tx);

            tx.setStatus(TransactionStatus.CREDIT_REQUESTED);
            repository.save(tx);
            return;
        }

        // HANDLE CREDIT RESPONSE
        if ("CREDIT".equals(event.getType())) {

            if ("FAILED".equals(event.getStatus())) {

                // Compensation case
                tx.setStatus(TransactionStatus.REVERSED);
                repository.save(tx);
                return;
            }

            // Credit success
            tx.setCreditCompleted(true);

            createLedgerEntries(tx);

            tx.setStatus(TransactionStatus.SUCCESS);
            repository.save(tx);
        }
    }

    private void sendCreditEvent(Transaction tx) {

        producer.sendEvent(
                BalanceEvent.builder()
                        .accountNumber(tx.getDestinationAccount())
                        .amount(tx.getAmount())
                        .transactionRef(tx.getTransactionRef())
                        .type("CREDIT")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

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
}