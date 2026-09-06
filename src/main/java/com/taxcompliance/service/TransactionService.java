package com.taxcompliance.service;

import com.taxcompliance.dto.request.TransactionBatchRequest;
import com.taxcompliance.dto.response.BatchUploadResponse;
import com.taxcompliance.dto.response.TransactionResponse;
import com.taxcompliance.entity.Transaction;
import com.taxcompliance.entity.TransactionBatch;
import com.taxcompliance.enums.AuditEventType;
import com.taxcompliance.enums.BatchProcessingStatus;
import com.taxcompliance.enums.ValidationStatus;
import com.taxcompliance.mapper.TransactionMapper;
import com.taxcompliance.repository.TransactionBatchRepository;
import com.taxcompliance.repository.TransactionRepository;
import com.taxcompliance.validation.TransactionValidationResult;
import com.taxcompliance.validation.TransactionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TransactionService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionService.class);

    private final TransactionBatchRepository transactionBatchRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionValidator transactionValidator;
    private final TransactionMapper transactionMapper;
    private final AuditLogService auditLogService;

    public TransactionService(
            TransactionBatchRepository transactionBatchRepository,
            TransactionRepository transactionRepository,
            TransactionValidator transactionValidator,
            TransactionMapper transactionMapper,
            AuditLogService auditLogService) {

        this.transactionBatchRepository = transactionBatchRepository;
        this.transactionRepository = transactionRepository;
        this.transactionValidator = transactionValidator;
        this.transactionMapper = transactionMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public BatchUploadResponse uploadBatch(TransactionBatchRequest request) {

        TransactionBatch batch = transactionBatchRepository.save(
                new TransactionBatch(
                        "BATCH-" + UUID.randomUUID(),
                        Instant.now()
                )
        );

        List<Transaction> transactions = new ArrayList<>();

        int successfulTransactions = 0;
        int failedTransactions = 0;

        for (Map<String, Object> rawTransaction : request.transactions()) {

            TransactionValidationResult validationResult =
                    transactionValidator.validate(rawTransaction);

            ValidationStatus validationStatus =
                    validationResult.successful()
                            ? ValidationStatus.SUCCESS
                            : ValidationStatus.FAILURE;

            if (validationStatus == ValidationStatus.SUCCESS) {

                successfulTransactions++;

            } else {

                failedTransactions++;

                LOGGER.warn(
                        "Transaction validation failed in batchReference={} transactionId={} failureCount={}",
                        batch.getBatchReference(),
                        validationResult.transaction().transactionId(),
                        validationResult.failureReasons().size()
                );
            }

            transactions.add(
                    transactionMapper.toEntity(
                            batch,
                            validationResult.transaction(),
                            validationStatus,
                            validationResult.failureReasons(),
                            rawTransaction
                    )
            );
        }

        batch.updateSummary(
                request.transactions().size(),
                successfulTransactions,
                failedTransactions
        );

        transactionBatchRepository.save(batch);

        List<Transaction> savedTransactions =
                transactionRepository.saveAll(transactions);

        /*
         * AUDIT LOGGING
         *
         * Create one INGESTION audit record for every
         * transaction that was successfully persisted.
         */
        for (Transaction transaction : savedTransactions) {

            Map<String, Object> details = new LinkedHashMap<>();

            details.put(
                    "action",
                    "TRANSACTION_INGESTED"
            );

            details.put(
                    "batchReference",
                    batch.getBatchReference()
            );

            details.put(
                    "validationStatus",
                    transaction.getValidationStatus() != null
                            ? transaction.getValidationStatus().name()
                            : null
            );

            details.put(
                    "customerId",
                    transaction.getCustomerId()
            );

            details.put(
                    "amount",
                    transaction.getAmount()
            );

            details.put(
                    "taxRate",
                    transaction.getTaxRate()
            );

            details.put(
                    "transactionType",
                    transaction.getTransactionType() != null
                            ? transaction.getTransactionType().name()
                            : null
            );

            details.put(
                    "failureReasons",
                    transaction.getFailureReasons()
            );

            auditLogService.record(
                    AuditEventType.INGESTION,
                    transaction.getTransactionId(),
                    details
            );
        }

        LOGGER.info(
                "Processed transaction batch batchReference={} totalTransactions={} successfulTransactions={} failedTransactions={}",
                batch.getBatchReference(),
                batch.getTotalTransactions(),
                batch.getSuccessfulTransactions(),
                batch.getFailedTransactions()
        );

        List<TransactionResponse> transactionResponses =
                savedTransactions.stream()
                        .map(transactionMapper::toResponse)
                        .toList();

        return new BatchUploadResponse(
                batch.getId(),
                batch.getBatchReference(),
                batch.getTotalTransactions(),
                batch.getSuccessfulTransactions(),
                batch.getFailedTransactions(),
                resolveBatchStatus(
                        successfulTransactions,
                        failedTransactions
                ),
                transactionResponses
        );
    }

    private BatchProcessingStatus resolveBatchStatus(
            int successfulTransactions,
            int failedTransactions) {

        if (failedTransactions == 0) {
            return BatchProcessingStatus.SUCCESS;
        }

        if (successfulTransactions == 0) {
            return BatchProcessingStatus.FAILURE;
        }

        return BatchProcessingStatus.PARTIAL_SUCCESS;
    }
}