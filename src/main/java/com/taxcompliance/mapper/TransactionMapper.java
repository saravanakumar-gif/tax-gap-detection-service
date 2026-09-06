package com.taxcompliance.mapper;

import com.taxcompliance.dto.request.TransactionRequest;
import com.taxcompliance.dto.response.TransactionResponse;
import com.taxcompliance.entity.Transaction;
import com.taxcompliance.entity.TransactionBatch;
import com.taxcompliance.enums.ValidationStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class TransactionMapper {

    public Transaction toEntity(
            TransactionBatch batch,
            TransactionRequest request,
            ValidationStatus validationStatus,
            List<String> failureReasons,
            Map<String, Object> rawTransaction) {
        return new Transaction(
                batch,
                request.transactionId(),
                request.date(),
                request.customerId(),
                request.amount(),
                request.taxRate(),
                request.reportedTax(),
                request.transactionType(),
                validationStatus,
                failureReasons,
                rawTransaction,
                Instant.now());
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getValidationStatus(),
                transaction.getFailureReasons());
    }
}
