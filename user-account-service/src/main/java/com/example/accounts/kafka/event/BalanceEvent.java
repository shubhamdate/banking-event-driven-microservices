package com.example.accounts.kafka.event;

import lombok.Builder;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceEvent {

    private String accountNumber;
    private BigDecimal amount;
    private String transactionRef;
    private String type;
    private LocalDateTime timestamp;
}