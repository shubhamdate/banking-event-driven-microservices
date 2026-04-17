package com.example.accounts.entity;


import com.example.accounts.enums.AccountAction;
import com.example.accounts.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "account_audit", schema = "db")
public class AccountAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")

    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AccountId", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "Action", nullable = false, length = 50)
    private AccountAction action;
    // CREATE / BLOCK / CLOSE / UNBLOCK

    @Enumerated(EnumType.STRING)
    @Column(name = "OldStatus", length = 20)
    private AccountStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "NewStatus", length = 20)
    private AccountStatus newStatus;

    @Column(name = "PerformedBy", nullable = false, length = 100)
    private String performedBy;

    @CreationTimestamp
    @Column(name = "PerformedAt", nullable = false, updatable = false)
    private Timestamp performedAt;
}
