package com.taxcompliance.dto.response;

import java.math.BigDecimal;

public record CustomerTaxSummaryResponse(

        String customerId,

        BigDecimal totalAmount,

        BigDecimal totalReportedTax,

        BigDecimal totalExpectedTax,

        BigDecimal totalTaxGap,

        BigDecimal complianceScore

) {
}