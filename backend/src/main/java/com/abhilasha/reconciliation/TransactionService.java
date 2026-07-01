package com.abhilasha.reconciliation;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    // Reads a CSV file and saves each row as a Transaction
    public int importCsv(MultipartFile file, TransactionSource source) throws Exception {
        List<Transaction> transactions = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        boolean firstLine = true;

        while ((line = reader.readLine()) != null) {
            // Skip the header row (the first line with column names)
            if (firstLine) {
                firstLine = false;
                continue;
            }

            // Split the line by commas into separate values
            String[] parts = line.split(",");

            Transaction t = new Transaction();
            t.setSource(source);
            t.setReference(parts[0]);
            t.setAmount(new BigDecimal(parts[1]));
            t.setTransactionDate(LocalDate.parse(parts[2]));
            t.setDescription(parts[3]);
            t.setStatus(TransactionStatus.UNMATCHED);

            transactions.add(t);
        }

        reader.close();

        // Save all transactions to the database at once
        repository.saveAll(transactions);

        return transactions.size();
    }
}