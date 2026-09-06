package com.taxcompliance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record TransactionBatchRequest(
        @NotNull(message = "transactions must be present")
        @Size(min = 1, message = "transactions must contain at least one transaction")
        List<Map<String, Object>> transactions) {
}
