package com.taxcompliance.dto.response;

import com.taxcompliance.enums.ValidationStatus;

import java.util.List;

public record TransactionResponse(
        String transactionId,
        ValidationStatus validationStatus,
        List<String> failureReasons) {
}
