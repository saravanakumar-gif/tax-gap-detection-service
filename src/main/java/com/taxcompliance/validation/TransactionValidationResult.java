package com.taxcompliance.validation;

import com.taxcompliance.dto.request.TransactionRequest;

import java.util.List;

public record TransactionValidationResult(
        TransactionRequest transaction,
        List<String> failureReasons) {

    public boolean successful() {
        return failureReasons.isEmpty();
    }
}
