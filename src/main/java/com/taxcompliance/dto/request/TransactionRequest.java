package com.taxcompliance.dto.request;

import com.taxcompliance.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        String transactionId,
        LocalDate date,
        String customerId,
        BigDecimal amount,
        BigDecimal taxRate,
        BigDecimal reportedTax,
        TransactionType transactionType) {
}
