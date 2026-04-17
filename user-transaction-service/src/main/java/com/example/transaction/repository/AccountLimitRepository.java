package com.example.transaction.repository;

import com.example.transaction.entity.AccountLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface AccountLimitRepository extends JpaRepository<AccountLimit, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountLimit a WHERE a.accountNumber = :accountNumber")
    Optional<AccountLimit> findByAccountNumberForUpdate(String accountNumber);

    Optional<AccountLimit> findByAccountNumber(String accountNumber);
}