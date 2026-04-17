package com.example.accounts.kafka;

import com.example.accounts.kafka.event.BalanceEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountEventProducer {

    private final KafkaTemplate<String, BalanceEvent> kafkaTemplate;

    private static final String TOPIC = "account-balance-events";

    public void publish(BalanceEvent event) {
        kafkaTemplate.send(TOPIC, event.getAccountNumber(), event);
    }
}