package com.taxcompliance.repository.projection;

import java.math.BigDecimal;

public interface CustomerTaxSummaryProjection {

    String getCustomerId();

    BigDecimal getTotalAmount();

    BigDecimal getTotalReportedTax();

    BigDecimal getTotalExpectedTax();

    BigDecimal getTotalTaxGap();

    Long getTotalTransactions();

    Long getNonCompliantTransactions();
}