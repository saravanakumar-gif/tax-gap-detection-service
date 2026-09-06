package com.taxcompliance.validation;

import com.taxcompliance.dto.request.TransactionRequest;
import com.taxcompliance.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TransactionValidator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
            .withResolverStyle(ResolverStyle.STRICT);

    public TransactionValidationResult validate(Map<String, Object> rawTransaction) {
        List<String> failureReasons = new ArrayList<>();

        String transactionId = textValue(rawTransaction, "transactionId");
        if (isBlank(transactionId)) {
            failureReasons.add("transactionId must be present and not blank");
        }

        LocalDate date = parseDate(rawTransaction, failureReasons);

        String customerId = textValue(rawTransaction, "customerId");
        if (isBlank(customerId)) {
            failureReasons.add("customerId must be present and not blank");
        }

        BigDecimal amount = decimalValue(rawTransaction, "amount", failureReasons);
        if (amount == null) {
            failureReasons.add("amount must be present");
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            failureReasons.add("amount must be greater than 0");
        }

        BigDecimal taxRate = decimalValue(rawTransaction, "taxRate", failureReasons);
        if (taxRate == null) {
            failureReasons.add("taxRate must be present");
        }

        BigDecimal reportedTax = decimalValue(rawTransaction, "reportedTax", failureReasons);
        if (reportedTax == null) {
            failureReasons.add("reportedTax must be present");
        }

        TransactionType transactionType = parseTransactionType(rawTransaction, failureReasons);

        return new TransactionValidationResult(
                new TransactionRequest(transactionId, date, customerId, amount, taxRate, reportedTax, transactionType),
                List.copyOf(failureReasons));
    }

    private LocalDate parseDate(Map<String, Object> rawTransaction, List<String> failureReasons) {
        Object dateValue = rawTransaction.get("date");
        if (!(dateValue instanceof String rawDate) || isBlank(rawDate)) {
            failureReasons.add("date must be present and valid in yyyy-MM-dd format");
            return null;
        }

        try {
            return LocalDate.parse(rawDate, DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            failureReasons.add("date must be valid in yyyy-MM-dd format");
            return null;
        }
    }

    private TransactionType parseTransactionType(Map<String, Object> rawTransaction, List<String> failureReasons) {
        String value = textValue(rawTransaction, "transactionType");
        if (isBlank(value)) {
            failureReasons.add("transactionType must be present");
            return null;
        }

        try {
            return TransactionType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            failureReasons.add("transactionType must be one of SALE, REFUND, EXPENSE");
            return null;
        }
    }

    private BigDecimal decimalValue(Map<String, Object> rawTransaction, String fieldName, List<String> failureReasons) {
        Object value = rawTransaction.get(fieldName);
        if (value == null) {
            return null;
        }
        if (!(value instanceof Number) && !(value instanceof String)) {
            failureReasons.add(fieldName + " must be a valid decimal value");
            return null;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException exception) {
            failureReasons.add(fieldName + " must be a valid decimal value");
            return null;
        }
    }

    private String textValue(Map<String, Object> rawTransaction, String fieldName) {
        Object value = rawTransaction.get(fieldName);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
