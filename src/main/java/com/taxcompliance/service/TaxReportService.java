package com.taxcompliance.service;

import com.taxcompliance.dto.response.CustomerTaxSummaryResponse;
import com.taxcompliance.repository.TaxReportRepository;
import com.taxcompliance.repository.projection.CustomerTaxSummaryProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class TaxReportService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final TaxReportRepository taxReportRepository;

    public TaxReportService(TaxReportRepository taxReportRepository) {
        this.taxReportRepository = taxReportRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerTaxSummaryResponse> getCustomerTaxSummary() {

        List<CustomerTaxSummaryProjection> summaries =
                taxReportRepository.findCustomerTaxSummary();

        return summaries.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CustomerTaxSummaryResponse mapToResponse(
            CustomerTaxSummaryProjection projection) {

        long totalTransactions =
                projection.getTotalTransactions() == null
                        ? 0L
                        : projection.getTotalTransactions();

        long nonCompliantTransactions =
                projection.getNonCompliantTransactions() == null
                        ? 0L
                        : projection.getNonCompliantTransactions();

        BigDecimal complianceScore =
                calculateComplianceScore(
                        totalTransactions,
                        nonCompliantTransactions
                );

        return new CustomerTaxSummaryResponse(
                projection.getCustomerId(),
                normalize(projection.getTotalAmount()),
                normalize(projection.getTotalReportedTax()),
                normalize(projection.getTotalExpectedTax()),
                normalize(projection.getTotalTaxGap()),
                complianceScore
        );
    }

    private BigDecimal calculateComplianceScore(
            long totalTransactions,
            long nonCompliantTransactions) {

        if (totalTransactions == 0) {
            return HUNDRED.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        BigDecimal nonCompliantPercentage =
                BigDecimal.valueOf(nonCompliantTransactions)
                        .multiply(HUNDRED)
                        .divide(
                                BigDecimal.valueOf(totalTransactions),
                                4,
                                RoundingMode.HALF_UP
                        );

        return HUNDRED
                .subtract(nonCompliantPercentage)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalize(BigDecimal value) {

        if (value == null) {
            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        return value.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }
}