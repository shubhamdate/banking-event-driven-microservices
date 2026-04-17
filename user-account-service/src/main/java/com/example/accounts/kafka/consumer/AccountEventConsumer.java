package com.example.accounts.kafka.consumer;

import com.example.accounts.dto.BalanceOperationRequest;
import com.example.accounts.kafka.event.BalanceEvent;
import com.example.accounts.kafka.event.BalanceResultEvent;
import com.example.accounts.kafka.BalanceResultProducer;
import com.example.accounts.service.AccountBalanceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AccountEventConsumer {

    private final AccountBalanceService balanceService;
    private final BalanceResultProducer resultProducer;

    private static final Logger log = LoggerFactory.getLogger(AccountEventConsumer.class);

    @KafkaListener(topics = "account-balance-events", groupId = "account-group")
    public void handleEvent(BalanceEvent event) {
        log.info("ACCOUNT RECEIVED EVENT: {}", event);

        try {

            BalanceOperationRequest request = map(event);

            if ("DEBIT".equals(event.getType())) {

                balanceService.debit(request);
            } else {

                balanceService.credit(request);
            }

            // SUCCESS EVENT
            resultProducer.send(
                    BalanceResultEvent.builder()
                            .transactionRef(event.getTransactionRef())
                            .accountNumber(event.getAccountNumber())
                            .type(event.getType())
                            .status("SUCCESS")
                            .timestamp(LocalDateTime.now())
                            .build()
            );

        } catch (Exception ex) {
            log.error("Failed processing event {}", event, ex);

            // FAILURE EVENT
            resultProducer.send(
                    BalanceResultEvent.builder()
                            .transactionRef(event.getTransactionRef())
                            .accountNumber(event.getAccountNumber())
                            .type(event.getType())
                            .status("FAILED")
                            .errorMessage(ex.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }
    }

    private BalanceOperationRequest map(BalanceEvent event) {
        BalanceOperationRequest request = new BalanceOperationRequest();
        request.setAccountNumber(event.getAccountNumber());
        request.setAmount(event.getAmount());
        request.setTransactionRef(event.getTransactionRef());
        return request;
    }
}
