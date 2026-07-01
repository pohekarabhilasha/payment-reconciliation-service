package com.abhilasha.reconciliation;

public class ReconciliationSummary {
    private int matched;
    private int discrepancies;
    private int unmatched;

    public ReconciliationSummary(int matched, int discrepancies, int unmatched) {
        this.matched = matched;
        this.discrepancies = discrepancies;
        this.unmatched = unmatched;
    }

    public int getMatched() { return matched; }
    public int getDiscrepancies() { return discrepancies; }
    public int getUnmatched() { return unmatched; }
}