package com.example.accounts.repository;

import com.example.accounts.entity.Account;
import com.example.accounts.entity.Customer;
import com.example.accounts.enums.AccountType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);

    Optional<Account> findByAccountNumberAndCustomer(String accountNumber, Customer customer);

    // LOCK ACCOUNT ROW FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT a FROM Account a
           WHERE a.accountNumber = :accountNumber
           """)
    Optional<Account> findByAccountNumberForUpdate(String accountNumber);

    @Query(value = "SELECT nextval('account_seq')", nativeQuery = true)
    Long getNextSequence();

    Optional<Account> findByCustomerIdAndAccountType(Long customerId, AccountType accountType);
}
