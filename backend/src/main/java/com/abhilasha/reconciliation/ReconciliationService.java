package com.abhilasha.reconciliation;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReconciliationService {

    private final TransactionRepository repository;

    public ReconciliationService(TransactionRepository repository) {
        this.repository = repository;
    }

    public ReconciliationSummary reconcile() {
        // Get all transactions from each source
        List<Transaction> internalList = repository.findBySource(TransactionSource.INTERNAL);
        List<Transaction> bankList = repository.findBySource(TransactionSource.BANK);

        int matched = 0;
        int discrepancies = 0;

        // Go through each internal transaction and look for a bank match
        for (Transaction internal : internalList) {
            Transaction bankMatch = null;

            // Find a bank transaction with the same reference
            for (Transaction bank : bankList) {
                if (bank.getReference().equals(internal.getReference())) {
                    bankMatch = bank;
                    break;
                }
            }

            if (bankMatch != null) {
                // Same reference found. Now check the amount.
                if (internal.getAmount().compareTo(bankMatch.getAmount()) == 0) {
                    // Amounts match too - fully matched
                    internal.setStatus(TransactionStatus.MATCHED);
                    bankMatch.setStatus(TransactionStatus.MATCHED);
                    matched++;
                } else {
                    // Reference matches but amount differs - discrepancy
                    internal.setStatus(TransactionStatus.DISCREPANCY);
                    bankMatch.setStatus(TransactionStatus.DISCREPANCY);
                    discrepancies++;
                }
            }
            // If no bank match, internal stays UNMATCHED (its default)
        }

        // Save all the updated statuses back to the database
        repository.saveAll(internalList);
        repository.saveAll(bankList);

        // Count how many are still unmatched (on both sides)
        long unmatched = repository.findByStatus(TransactionStatus.UNMATCHED).size();

        return new ReconciliationSummary(matched, discrepancies, (int) unmatched);
    }
}