package com.example.transaction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "account_limit", schema = "db")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(name = "daily_limit", nullable = false, precision = 19, scale = 4)
    private BigDecimal dailyLimit;

    @Column(name = "per_tx_limit", nullable = false, precision = 19, scale = 4)
    private BigDecimal perTxLimit;

    @Column(name = "used_today", nullable = false, precision = 19, scale = 4)
    private BigDecimal usedToday;

    @Column(name = "last_reset", nullable = false)
    private LocalDate lastReset;
}