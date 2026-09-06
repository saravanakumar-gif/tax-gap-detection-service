package com.taxcompliance.validation;

import com.taxcompliance.enums.TransactionType;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionValidatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TransactionValidator validator = new TransactionValidator();

    @Test
    void validTransactionPassesValidation() throws IOException {
        TransactionValidationResult result = validator.validate(transaction("""
                {
                  "transactionId": "TXN-1001",
                  "date": "2026-09-05",
                  "customerId": "CUST-001",
                  "amount": 10000.00,
                  "taxRate": 18.00,
                  "reportedTax": 1800.00,
                  "transactionType": "SALE"
                }
                """));

        assertTrue(result.successful());
        assertEquals("TXN-1001", result.transaction().transactionId());
        assertEquals(0, new BigDecimal("10000.00").compareTo(result.transaction().amount()));
        assertEquals(TransactionType.SALE, result.transaction().transactionType());
    }

    @Test
    void missingTransactionIdFailsValidation() throws IOException {
        Map<String, Object> transaction = validTransaction();
        transaction.remove("transactionId");

        TransactionValidationResult result = validator.validate(transaction);

        assertEquals("transactionId must be present and not blank", result.failureReasons().get(0));
    }

    @Test
    void blankTransactionIdFailsValidation() throws IOException {
        Map<String, Object> transaction = validTransaction();
        transaction.put("transactionId", " ");

        TransactionValidationResult result = validator.validate(transaction);

        assertTrue(result.failureReasons().contains("transactionId must be present and not blank"));
    }

    @Test
    void missingDateFailsValidation() throws IOException {
        Map<String, Object> transaction = validTransaction();
        transaction.remove("date");

        TransactionValidationResult result = validator.validate(transaction);

        assertTrue(result.failureReasons().contains("date must be present and valid in yyyy-MM-dd format"));
    }

    @Test
    void invalidDateFormatIsHandledAsTransactionLevelFailure() throws IOException {
        Map<String, Object> transaction = validTransaction();
        transaction.put("date", "05-09-2026");

        TransactionValidationResult result = validator.validate(transaction);

        assertNull(result.transaction().date());
        assertTrue(result.failureReasons().contains("date must be valid in yyyy-MM-dd format"));
    }

    @Test
    void missingCustomerIdFailsValidation() throws IOException {
        Map<String, Object> transaction = validTransaction();
        transaction.remove("customerId");

        TransactionValidationResult result = validator.validate(transaction);

        assertTrue(result.failureReasons().contains("customerId must be present and not blank"));
    }

    @Test
    void blankCustomerIdFailsValidation() throws IOException {
        Map<String, Object> transaction = validTransaction();
        transaction.put("customerId", "");

        TransactionValidationResult result = validator.validate(transaction);

        assertTrue(result.failureReasons().contains("customerId must be present and not blank"));
    }

    @Test
    void missingAmountFailsValidation() throws IOException {
        Map<String, Object> transaction = validTransaction();
        transaction.remove("amount");

        TransactionValidationResult result = validator.validate(transaction);

        assertTrue(result.failureReasons().contains("amount must be present"));
    }

    @Test
    void zeroAmountFailsValidation() throws IOException {
        Map<String, Object> transaction = validTransaction();
        transaction.put("amount", 0);

        TransactionValidationResult result = validator.validate(transaction);

        assertTrue(result.failureReasons().contains("amount must be greater than 0"));
    }

    @Test
    void negativeAmountFailsValidation() throws IOException {
        Map<String, Object> transaction = validTransaction();
        transaction.put("amount", -1);

        TransactionValidationResult result = validator.validate(transaction);

        assertTrue(result.failureReasons().contains("amount must be greater than 0"));
    }

    @Test
    void missingTaxRateFailsValidation() throws IOException {
        Map<String, Object> transaction = validTransaction();
        transaction.remove("taxRate");

        TransactionValidationResult result = validator.validate(transaction);

        assertTrue(result.failureReasons().contains("taxRate must be present"));
    }

    @Test
    void missingReportedTaxFailsValidation() throws IOException {
        Map<String, Object> transaction = validTransaction();
        transaction.remove("reportedTax");

        TransactionValidationResult result = validator.validate(transaction);

        assertTrue(result.failureReasons().contains("reportedTax must be present"));
    }

    @Test
    void missingTransactionTypeFailsValidation() throws IOException {
        Map<String, Object> transaction = validTransaction();
        transaction.remove("transactionType");

        TransactionValidationResult result = validator.validate(transaction);

        assertTrue(result.failureReasons().contains("transactionType must be present"));
    }

    @Test
    void invalidTransactionTypeIsHandledCorrectly() throws IOException {
        Map<String, Object> transaction = validTransaction();
        transaction.put("transactionType", "TRANSFER");

        TransactionValidationResult result = validator.validate(transaction);

        assertNull(result.transaction().transactionType());
        assertTrue(result.failureReasons().contains("transactionType must be one of SALE, REFUND, EXPENSE"));
    }

    @Test
    void multipleValidationErrorsAreCollected() throws IOException {
        Map<String, Object> transaction = validTransaction();
        transaction.put("transactionId", "");
        transaction.put("date", "abc");
        transaction.put("amount", -100);
        transaction.put("transactionType", "UNKNOWN");

        TransactionValidationResult result = validator.validate(transaction);

        assertEquals(4, result.failureReasons().size());
    }

    private Map<String, Object> validTransaction() throws IOException {
        return transaction("""
                {
                  "transactionId": "TXN-1001",
                  "date": "2026-09-05",
                  "customerId": "CUST-001",
                  "amount": 10000.00,
                  "taxRate": 18.00,
                  "reportedTax": 1800.00,
                  "transactionType": "SALE"
                }
                """);
    }

    private Map<String, Object> transaction(String json) throws IOException {
        return objectMapper.readValue(json, new TypeReference<>() {
        });
    }
}
