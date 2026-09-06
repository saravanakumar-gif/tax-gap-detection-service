package com.taxcompliance.service;

import com.taxcompliance.dto.response.TaxCalculationBatchResponse;
import com.taxcompliance.dto.response.TaxResultResponse;
import com.taxcompliance.entity.TaxResult;
import com.taxcompliance.entity.Transaction;
import com.taxcompliance.entity.TransactionBatch;
import com.taxcompliance.enums.ComplianceStatus;
import com.taxcompliance.enums.ValidationStatus;
import com.taxcompliance.exception.ResourceNotFoundException;
import com.taxcompliance.repository.TaxResultRepository;
import com.taxcompliance.repository.TransactionBatchRepository;
import com.taxcompliance.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TaxCalculationService {

    private static final BigDecimal COMPLIANCE_THRESHOLD =
            BigDecimal.ONE;

    private final TransactionRepository transactionRepository;
    private final TransactionBatchRepository transactionBatchRepository;
    private final TaxResultRepository taxResultRepository;

    public TaxCalculationService(
            TransactionRepository transactionRepository,
            TransactionBatchRepository transactionBatchRepository,
            TaxResultRepository taxResultRepository) {

        this.transactionRepository = transactionRepository;
        this.transactionBatchRepository = transactionBatchRepository;
        this.taxResultRepository = taxResultRepository;
    }

    @Transactional
    public TaxCalculationBatchResponse calculateForBatch(
            UUID batchId) {

        TransactionBatch batch =
                transactionBatchRepository.findById(batchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transaction batch not found: "
                                                + batchId
                                )
                        );

        List<Transaction> validTransactions =
                transactionRepository
                        .findByBatchIdAndValidationStatus(
                                batchId,
                                ValidationStatus.SUCCESS
                        );

        List<TaxResultResponse> results =
                new ArrayList<>();

        int compliantCount = 0;
        int underpaidCount = 0;
        int overpaidCount = 0;
        int nonCompliantCount = 0;

        for (Transaction transaction : validTransactions) {

            TaxResult taxResult =
                    calculateTax(transaction);

            TaxResult savedTaxResult =
                    taxResultRepository.save(taxResult);

            results.add(toResponse(savedTaxResult));

            switch (savedTaxResult.getComplianceStatus()) {

                case COMPLIANT -> compliantCount++;

                case UNDERPAID -> underpaidCount++;

                case OVERPAID -> overpaidCount++;

                case NON_COMPLIANT -> nonCompliantCount++;
            }
        }

        return new TaxCalculationBatchResponse(
                batch.getId(),
                validTransactions.size(),
                results.size(),
                compliantCount,
                underpaidCount,
                overpaidCount,
                nonCompliantCount,
                results
        );
    }

    private TaxResult calculateTax(
            Transaction transaction) {

        BigDecimal amount =
                transaction.getAmount();

        BigDecimal taxRate =
                transaction.getTaxRate();

        BigDecimal reportedTax =
                transaction.getReportedTax();

        BigDecimal expectedTax =
                amount.multiply(taxRate);

        /*
         * Requirement 2 explicitly says:
         *
         * reportedTax missing
         *      -> NON_COMPLIANT
         */
        if (reportedTax == null) {

            TaxResult existingResult =
                    taxResultRepository
                            .findByTransactionId(
                                    transaction.getId()
                            )
                            .orElse(null);

            if (existingResult != null) {

                existingResult.updateCalculation(
                        expectedTax,
                        null,
                        ComplianceStatus.NON_COMPLIANT,
                        Instant.now()
                );

                return existingResult;
            }

            return new TaxResult(
                    transaction,
                    expectedTax,
                    null,
                    ComplianceStatus.NON_COMPLIANT,
                    Instant.now()
            );
        }

        BigDecimal taxGap =
                expectedTax.subtract(reportedTax);

        ComplianceStatus complianceStatus =
                determineComplianceStatus(taxGap);

        TaxResult existingResult =
                taxResultRepository
                        .findByTransactionId(
                                transaction.getId()
                        )
                        .orElse(null);

        if (existingResult != null) {

            existingResult.updateCalculation(
                    expectedTax,
                    taxGap,
                    complianceStatus,
                    Instant.now()
            );

            return existingResult;
        }

        return new TaxResult(
                transaction,
                expectedTax,
                taxGap,
                complianceStatus,
                Instant.now()
        );
    }

    private ComplianceStatus determineComplianceStatus(
            BigDecimal taxGap) {

        /*
         * |taxGap| <= 1
         *     -> COMPLIANT
         */
        if (taxGap.abs().compareTo(
                COMPLIANCE_THRESHOLD
        ) <= 0) {

            return ComplianceStatus.COMPLIANT;
        }

        /*
         * taxGap > 1
         *     -> UNDERPAID
         */
        if (taxGap.compareTo(
                BigDecimal.ONE
        ) > 0) {

            return ComplianceStatus.UNDERPAID;
        }

        /*
         * taxGap < -1
         *     -> OVERPAID
         */
        return ComplianceStatus.OVERPAID;
    }

    private TaxResultResponse toResponse(
            TaxResult taxResult) {

        Transaction transaction =
                taxResult.getTransaction();

        return new TaxResultResponse(
                taxResult.getId(),
                transaction.getId(),
                transaction.getTransactionId(),
                taxResult.getExpectedTax(),
                transaction.getReportedTax(),
                taxResult.getTaxGap(),
                taxResult.getComplianceStatus(),
                taxResult.getCalculatedAt()
        );
    }
}