package com.example.accounts.repository;

import com.example.accounts.entity.Account;
import com.example.accounts.entity.AccountAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountAuditRepository extends JpaRepository<AccountAudit, Long> {

    List<AccountAudit> findByAccount(Account account);

    List<AccountAudit> findByAccountId(Long accountId);
}

