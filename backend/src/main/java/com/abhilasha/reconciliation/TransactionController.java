package com.abhilasha.reconciliation;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin
public class TransactionController {

    private final TransactionService service;
    private final TransactionRepository repository;

    public TransactionController(TransactionService service, TransactionRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    // Upload a CSV file. Pass the source as a URL parameter: INTERNAL or BANK
    @PostMapping("/import")
    public String importCsv(@RequestParam("file") MultipartFile file,
                            @RequestParam("source") TransactionSource source) {
        try {
            int count = service.importCsv(file, source);
            return "Imported " + count + " transactions from " + source;
        } catch (Exception e) {
            return "Error importing file: " + e.getMessage();
        }
    }

    // Get all transactions (to check what's in the database)
    @GetMapping
    public List<Transaction> getAll() {
        return repository.findAll();
    }

    // Get all matched transactions
    @GetMapping("/matched")
    public List<Transaction> getMatched() {
        return repository.findByStatus(TransactionStatus.MATCHED);
    }

    // Get all unmatched transactions (missing on one side)
    @GetMapping("/unmatched")
    public List<Transaction> getUnmatched() {
        return repository.findByStatus(TransactionStatus.UNMATCHED);
    }

    // Get all discrepancies (reference matches but amount differs)
    @GetMapping("/discrepancies")
    public List<Transaction> getDiscrepancies() {
        return repository.findByStatus(TransactionStatus.DISCREPANCY);
    }

    // Delete all transactions (for a fresh reconciliation run)
    @DeleteMapping("/clear")
    public String clearAll() {
        repository.deleteAll();
        return "All transactions cleared";
    }
}