package com.taxcompliance.dto.response;

import java.util.List;
import java.util.UUID;

public record TaxCalculationBatchResponse(
        UUID batchId,
        int totalValidTransactions,
        int calculatedTransactions,
        int compliantTransactions,
        int underpaidTransactions,
        int overpaidTransactions,
        int nonCompliantTransactions,
        List<TaxResultResponse> results
) {
}