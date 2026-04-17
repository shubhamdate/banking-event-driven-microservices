package com.example.accounts.repository;

import com.example.accounts.entity.AccountAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountAuditRepository extends JpaRepository<AccountAudit, Long> {
}

