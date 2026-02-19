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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AccountAction action;
    // CREATE / BLOCK / CLOSE / UNBLOCK

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private AccountStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 20)
    private AccountStatus newStatus;

    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;
    // userId or SYSTEM

    @CreationTimestamp
    @Column(name = "performed_at", nullable = false, updatable = false)
    private Timestamp performedAt;
}
