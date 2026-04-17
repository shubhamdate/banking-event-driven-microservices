package com.example.transaction.repository;

import com.example.transaction.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {

    boolean existsById(String id);

    Optional<IdempotencyKey> findById(String id);
}