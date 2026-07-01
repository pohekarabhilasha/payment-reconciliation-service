package com.abhilasha.reconciliation;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reconciliation")
@CrossOrigin
public class ReconciliationController {

    private final ReconciliationService service;

    public ReconciliationController(ReconciliationService service) {
        this.service = service;
    }

    // Run the reconciliation and return the summary
    @PostMapping("/run")
    public ReconciliationSummary run() {
        return service.reconcile();
    }
}