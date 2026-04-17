package com.example.accounts.kafka;

import com.example.accounts.kafka.event.BalanceResultEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BalanceResultProducer {

    private final KafkaTemplate<String, BalanceResultEvent> kafkaTemplate;

    private static final String TOPIC = "account-balance-response";

    public void send(BalanceResultEvent event) {
        kafkaTemplate.send(TOPIC, event.getTransactionRef(), event);
    }
}