package com.taxcompliance.repository;

import com.taxcompliance.entity.Transaction;
import com.taxcompliance.repository.projection.CustomerTaxSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TaxReportRepository
        extends JpaRepository<Transaction, UUID> {

    @Query("""
            SELECT
                t.customerId AS customerId,

                COALESCE(SUM(t.amount), 0) AS totalAmount,

                COALESCE(SUM(t.reportedTax), 0) AS totalReportedTax,

                COALESCE(SUM(tr.expectedTax), 0) AS totalExpectedTax,

                COALESCE(SUM(tr.taxGap), 0) AS totalTaxGap,

                COUNT(t.id) AS totalTransactions,

                SUM(
                    CASE
                        WHEN tr.complianceStatus <> com.taxcompliance.enums.ComplianceStatus.COMPLIANT
                        THEN 1
                        ELSE 0
                    END
                ) AS nonCompliantTransactions

            FROM Transaction t
            JOIN TaxResult tr
                ON tr.transaction.id = t.id

            WHERE t.validationStatus =
                com.taxcompliance.enums.ValidationStatus.SUCCESS

            GROUP BY t.customerId

            ORDER BY t.customerId
            """)
    List<CustomerTaxSummaryProjection> findCustomerTaxSummary();
}