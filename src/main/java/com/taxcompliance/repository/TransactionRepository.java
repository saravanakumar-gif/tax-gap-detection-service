package com.taxcompliance.repository;

import com.taxcompliance.entity.Transaction;
import com.taxcompliance.enums.TransactionType;
import com.taxcompliance.enums.ValidationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByBatchId(UUID batchId);

    long countByBatchIdAndValidationStatus(UUID batchId, ValidationStatus validationStatus);

    List<Transaction> findByBatchIdAndValidationStatus(
            UUID batchId,
            ValidationStatus validationStatus
    );

    Optional<Transaction> findFirstByTransactionIdAndTransactionTypeAndValidationStatus(
            String transactionId,
            TransactionType transactionType,
            ValidationStatus validationStatus
    );
}
