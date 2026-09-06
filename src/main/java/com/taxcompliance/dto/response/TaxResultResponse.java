package com.taxcompliance.dto.response;

import com.taxcompliance.enums.ComplianceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TaxResultResponse(
        UUID taxResultId,
        UUID transactionId,
        String businessTransactionId,
        BigDecimal expectedTax,
        BigDecimal reportedTax,
        BigDecimal taxGap,
        ComplianceStatus complianceStatus,
        Instant calculatedAt
) {
}