package com.taxcompliance.dto.response;

import com.taxcompliance.enums.BatchProcessingStatus;

import java.util.List;
import java.util.UUID;

public record BatchUploadResponse(
        UUID batchId,
        String batchReference,
        int totalTransactions,
        int successfulTransactions,
        int failedTransactions,
        BatchProcessingStatus status,
        List<TransactionResponse> transactions) {
}
