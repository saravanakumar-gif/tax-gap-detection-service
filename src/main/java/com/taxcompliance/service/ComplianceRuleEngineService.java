package com.taxcompliance.service;

import com.taxcompliance.dto.response.ComplianceRuleExecutionResponse;
import com.taxcompliance.dto.response.RuleExceptionResponse;
import com.taxcompliance.entity.ComplianceRule;
import com.taxcompliance.entity.ExceptionRecord;
import com.taxcompliance.entity.Transaction;
import com.taxcompliance.enums.ExceptionSeverity;
import com.taxcompliance.enums.RuleType;
import com.taxcompliance.enums.TransactionType;
import com.taxcompliance.enums.ValidationStatus;
import com.taxcompliance.exception.ResourceNotFoundException;
import com.taxcompliance.mapper.ExceptionRecordMapper;
import com.taxcompliance.repository.ComplianceRuleRepository;
import com.taxcompliance.repository.ExceptionRecordRepository;
import com.taxcompliance.repository.TransactionBatchRepository;
import com.taxcompliance.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ComplianceRuleEngineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComplianceRuleEngineService.class);

    private final TransactionBatchRepository transactionBatchRepository;
    private final TransactionRepository transactionRepository;
    private final ComplianceRuleRepository complianceRuleRepository;
    private final ExceptionRecordRepository exceptionRecordRepository;
    private final ExceptionRecordMapper exceptionRecordMapper;

    public ComplianceRuleEngineService(
            TransactionBatchRepository transactionBatchRepository,
            TransactionRepository transactionRepository,
            ComplianceRuleRepository complianceRuleRepository,
            ExceptionRecordRepository exceptionRecordRepository,
            ExceptionRecordMapper exceptionRecordMapper) {
        this.transactionBatchRepository = transactionBatchRepository;
        this.transactionRepository = transactionRepository;
        this.complianceRuleRepository = complianceRuleRepository;
        this.exceptionRecordRepository = exceptionRecordRepository;
        this.exceptionRecordMapper = exceptionRecordMapper;
    }

    @Transactional
    public ComplianceRuleExecutionResponse executeForBatch(UUID batchId) {
        if (!transactionBatchRepository.existsById(batchId)) {
            throw new ResourceNotFoundException("Transaction batch not found: " + batchId);
        }

        List<ComplianceRule> activeRules = complianceRuleRepository.findByEnabledTrueOrderByRuleCodeAsc();
        List<Transaction> transactions = transactionRepository.findByBatchId(batchId);

        exceptionRecordRepository.deleteByTransactionBatchId(batchId);

        List<ExceptionRecord> exceptions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            for (ComplianceRule rule : activeRules) {
                RuleEvaluationResult result = evaluate(rule, transaction);
                if (result.violation()) {
                    exceptions.add(new ExceptionRecord(
                            transaction,
                            rule,
                            result.severity(),
                            result.message(),
                            result.details(),
                            Instant.now()));
                }
            }
        }

        List<ExceptionRecord> savedExceptions = exceptionRecordRepository.saveAll(exceptions);
        LOGGER.info(
                "Executed compliance rules batchId={} evaluatedTransactions={} activeRules={} exceptionsCreated={}",
                batchId,
                transactions.size(),
                activeRules.size(),
                savedExceptions.size());

        List<RuleExceptionResponse> responses = savedExceptions.stream()
                .map(exceptionRecordMapper::toResponse)
                .toList();

        return new ComplianceRuleExecutionResponse(
                batchId,
                transactions.size(),
                activeRules.size(),
                savedExceptions.size(),
                responses);
    }

    private RuleEvaluationResult evaluate(ComplianceRule rule, Transaction transaction) {
        return switch (rule.getRuleType()) {
            case HIGH_VALUE_TRANSACTION -> evaluateHighValueTransaction(rule, transaction);
            case REFUND_VALIDATION -> evaluateRefundValidation(rule, transaction);
            case GST_SLAB_VIOLATION -> evaluateGstSlabViolation(rule, transaction);
        };
    }

    private RuleEvaluationResult evaluateHighValueTransaction(ComplianceRule rule, Transaction transaction) {
        BigDecimal threshold = decimalConfig(rule, "threshold");
        if (threshold == null || transaction.getAmount() == null || transaction.getAmount().compareTo(threshold) <= 0) {
            return RuleEvaluationResult.noViolation();
        }
        return RuleEvaluationResult.violation(
                severity(rule, ExceptionSeverity.HIGH),
                "Transaction amount exceeds configured high-value threshold",
                Map.of("amount", transaction.getAmount(), "threshold", threshold));
    }

    private RuleEvaluationResult evaluateRefundValidation(ComplianceRule rule, Transaction transaction) {
        if (transaction.getTransactionType() != TransactionType.REFUND || transaction.getAmount() == null) {
            return RuleEvaluationResult.noViolation();
        }

        String referenceField = stringConfig(rule, "originalSaleTransactionIdField", "originalTransactionId");
        Object originalTransactionId = transaction.getRawTransaction().get(referenceField);
        if (originalTransactionId == null || originalTransactionId.toString().isBlank()) {
            return RuleEvaluationResult.violation(
                    severity(rule, ExceptionSeverity.MEDIUM),
                    "Refund transaction is missing original sale transaction reference",
                    Map.of("referenceField", referenceField));
        }

        Transaction originalSale = transactionRepository
                .findFirstByTransactionIdAndTransactionTypeAndValidationStatus(
                        originalTransactionId.toString(),
                        TransactionType.SALE,
                        ValidationStatus.SUCCESS)
                .orElse(null);

        if (originalSale == null) {
            return RuleEvaluationResult.violation(
                    severity(rule, ExceptionSeverity.MEDIUM),
                    "Referenced original sale transaction was not found",
                    Map.of("originalTransactionId", originalTransactionId.toString()));
        }

        if (transaction.getAmount().compareTo(originalSale.getAmount()) <= 0) {
            return RuleEvaluationResult.noViolation();
        }

        return RuleEvaluationResult.violation(
                severity(rule, ExceptionSeverity.HIGH),
                "Refund amount exceeds original sale amount",
                Map.of(
                        "refundAmount", transaction.getAmount(),
                        "originalSaleAmount", originalSale.getAmount(),
                        "originalTransactionId", originalSale.getTransactionId()));
    }

    private RuleEvaluationResult evaluateGstSlabViolation(ComplianceRule rule, Transaction transaction) {
        BigDecimal slabThreshold = decimalConfig(rule, "slabThreshold");
        BigDecimal requiredTaxRate = decimalConfig(rule, "requiredTaxRate");
        if (slabThreshold == null
                || requiredTaxRate == null
                || transaction.getAmount() == null
                || transaction.getTaxRate() == null
                || transaction.getAmount().compareTo(slabThreshold) <= 0
                || transaction.getTaxRate().compareTo(requiredTaxRate) >= 0) {
            return RuleEvaluationResult.noViolation();
        }

        return RuleEvaluationResult.violation(
                severity(rule, ExceptionSeverity.HIGH),
                "GST slab violation: tax rate is below the configured slab requirement",
                Map.of(
                        "amount", transaction.getAmount(),
                        "slabThreshold", slabThreshold,
                        "actualTaxRate", transaction.getTaxRate(),
                        "requiredTaxRate", requiredTaxRate));
    }

    private BigDecimal decimalConfig(ComplianceRule rule, String key) {
        Object value = rule.getConfiguration().get(key);
        if (value == null) {
            return null;
        }
        return new BigDecimal(value.toString());
    }

    private String stringConfig(ComplianceRule rule, String key, String defaultValue) {
        Object value = rule.getConfiguration().get(key);
        if (value == null || value.toString().isBlank()) {
            return defaultValue;
        }
        return value.toString();
    }

    private ExceptionSeverity severity(ComplianceRule rule, ExceptionSeverity defaultSeverity) {
        Object configuredSeverity = rule.getConfiguration().get("severity");
        if (configuredSeverity == null) {
            return defaultSeverity;
        }
        try {
            return ExceptionSeverity.valueOf(configuredSeverity.toString());
        } catch (IllegalArgumentException exception) {
            return defaultSeverity;
        }
    }
}
