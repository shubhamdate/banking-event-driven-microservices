package com.example.transaction.kafka;

import com.example.transaction.dto.BalanceEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BalanceEventProducer {

    private final KafkaTemplate<String, BalanceEvent> kafkaTemplate;

    private static final String TOPIC = "account-balance-events";

    public void sendEvent(BalanceEvent event) {
        kafkaTemplate.send(TOPIC, event.getAccountNumber(), event);
    }
}