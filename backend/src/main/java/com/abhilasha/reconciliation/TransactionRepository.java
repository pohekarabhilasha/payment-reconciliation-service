package com.abhilasha.reconciliation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions from one source (INTERNAL or BANK)
    List<Transaction> findBySource(TransactionSource source);

    // Find all transactions with a given status (UNMATCHED, MATCHED, etc.)
    List<Transaction> findByStatus(TransactionStatus status);
}