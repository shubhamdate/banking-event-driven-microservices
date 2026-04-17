package com.example.accounts.entity;

import com.example.accounts.enums.AccountStatus;
import com.example.accounts.enums.AccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(
        name = "account",
        schema = "db",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"customer_id", "account_type"})
        }
)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "AccountNumber", nullable = false, unique = true, length = 25, updatable = false)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "AccountType", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "Currency", nullable = false, length = 10)
    private String currency;

    @Column(name= "Balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private AccountStatus status;

    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private Timestamp updatedAt;
}