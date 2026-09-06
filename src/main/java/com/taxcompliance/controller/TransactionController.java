package com.taxcompliance.controller;

import com.taxcompliance.dto.request.TransactionBatchRequest;
import com.taxcompliance.dto.response.BatchUploadResponse;
import com.taxcompliance.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchUploadResponse> uploadBatch(@Valid @RequestBody TransactionBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.uploadBatch(request));
    }
}
