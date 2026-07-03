package com.abhilasha.reconciliation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private ReconciliationService service;

    // Helper to build a transaction quickly
    private Transaction makeTransaction(TransactionSource source, String reference, String amount) {
        Transaction t = new Transaction();
        t.setSource(source);
        t.setReference(reference);
        t.setAmount(new BigDecimal(amount));
        t.setTransactionDate(LocalDate.of(2026, 6, 29));
        t.setStatus(TransactionStatus.UNMATCHED);
        return t;
    }

    @Test
    void matchingReferenceAndAmount_shouldBeMatched() {
        // Same reference, same amount on both sides -> MATCHED
        Transaction internal = makeTransaction(TransactionSource.INTERNAL, "TXN001", "100.00");
        Transaction bank = makeTransaction(TransactionSource.BANK, "TXN001", "100.00");

        when(repository.findBySource(TransactionSource.INTERNAL)).thenReturn(List.of(internal));
        when(repository.findBySource(TransactionSource.BANK)).thenReturn(List.of(bank));
        when(repository.findByStatus(TransactionStatus.UNMATCHED)).thenReturn(List.of());

        ReconciliationSummary summary = service.reconcile();

        assertEquals(1, summary.getMatched());
        assertEquals(0, summary.getDiscrepancies());
        assertEquals(TransactionStatus.MATCHED, internal.getStatus());
        assertEquals(TransactionStatus.MATCHED, bank.getStatus());
    }

    @Test
    void matchingReferenceDifferentAmount_shouldBeDiscrepancy() {
        // Same reference, different amount -> DISCREPANCY
        Transaction internal = makeTransaction(TransactionSource.INTERNAL, "TXN002", "300.00");
        Transaction bank = makeTransaction(TransactionSource.BANK, "TXN002", "320.00");

        when(repository.findBySource(TransactionSource.INTERNAL)).thenReturn(List.of(internal));
        when(repository.findBySource(TransactionSource.BANK)).thenReturn(List.of(bank));
        when(repository.findByStatus(TransactionStatus.UNMATCHED)).thenReturn(List.of());

        ReconciliationSummary summary = service.reconcile();

        assertEquals(0, summary.getMatched());
        assertEquals(1, summary.getDiscrepancies());
        assertEquals(TransactionStatus.DISCREPANCY, internal.getStatus());
        assertEquals(TransactionStatus.DISCREPANCY, bank.getStatus());
    }

    @Test
    void noMatchingBankTransaction_shouldStayUnmatched() {
        // Internal transaction with no bank counterpart -> stays UNMATCHED
        Transaction internal = makeTransaction(TransactionSource.INTERNAL, "TXN003", "75.00");

        when(repository.findBySource(TransactionSource.INTERNAL)).thenReturn(List.of(internal));
        when(repository.findBySource(TransactionSource.BANK)).thenReturn(List.of());
        when(repository.findByStatus(TransactionStatus.UNMATCHED)).thenReturn(List.of(internal));

        ReconciliationSummary summary = service.reconcile();

        assertEquals(0, summary.getMatched());
        assertEquals(0, summary.getDiscrepancies());
        assertEquals(TransactionStatus.UNMATCHED, internal.getStatus());
    }
}