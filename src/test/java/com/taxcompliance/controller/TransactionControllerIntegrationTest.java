package com.taxcompliance.controller;

import com.taxcompliance.dto.response.BatchUploadResponse;
import com.taxcompliance.entity.Transaction;
import com.taxcompliance.enums.ValidationStatus;
import com.taxcompliance.repository.TransactionBatchRepository;
import com.taxcompliance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import com.taxcompliance.config.TestSecurityConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
class TransactionControllerIntegrationTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AUTHORIZATION_HEADER = "Basic "
            + Base64.getEncoder().encodeToString("auditor:password".getBytes(StandardCharsets.UTF_8));
    private static final String PASSWORD_HASH =
            "$2a$10$GM/fV7dS.FI.MulbjcEFu.7kYo3sWHMlARx2eOib0YZIgW77SXqLq";

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionBatchRepository transactionBatchRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        seedAuditorUser();
        transactionRepository.deleteAll();
        transactionBatchRepository.deleteAll();
    }

    private void seedAuditorUser() {
        jdbcTemplate.update("""
                INSERT INTO app_users (id, username, password, role, enabled)
                VALUES (?, 'auditor', ?, 'AUDITOR', true)
                ON CONFLICT (username) DO UPDATE
                SET password = EXCLUDED.password,
                    role = EXCLUDED.role,
                    enabled = EXCLUDED.enabled
                """, UUID.randomUUID(), PASSWORD_HASH);
    }

    @Test
    void uploadBatchWithAllValidTransactionsPersistsSuccesses() throws Exception {
        HttpResponse<String> responseEntity = postBatch("""
                {
                  "transactions": [
                    {
                      "transactionId": "TXN-1001",
                      "date": "2026-09-01",
                      "customerId": "CUST-001",
                      "amount": 10000.00,
                      "taxRate": 18.00,
                      "reportedTax": 1800.00,
                      "transactionType": "SALE"
                    }
                  ]
                }
                """);

        assertEquals(201, responseEntity.statusCode());
        BatchUploadResponse response = objectMapper.readValue(responseEntity.body(), BatchUploadResponse.class);
        assertEquals("SUCCESS", response.status().name());
        assertEquals(1, response.totalTransactions());
        assertEquals(1, response.successfulTransactions());
        assertEquals(0, response.failedTransactions());
        List<Transaction> transactions = transactionRepository.findByBatchId(response.batchId());

        assertEquals(1, transactions.size());
        assertEquals(ValidationStatus.SUCCESS, transactions.get(0).getValidationStatus());
        assertTrue(transactions.get(0).getFailureReasons().isEmpty());
        assertEquals("TXN-1001", transactions.get(0).getRawTransaction().get("transactionId"));
    }

    @Test
    void uploadBatchWithMixedTransactionsPersistsSuccessesAndFailures() throws Exception {
        HttpResponse<String> responseEntity = postBatch("""
                {
                  "transactions": [
                    {
                      "transactionId": "TXN-1001",
                      "date": "2026-09-01",
                      "customerId": "CUST-001",
                      "amount": 10000.00,
                      "taxRate": 18.00,
                      "reportedTax": 1800.00,
                      "transactionType": "SALE"
                    },
                    {
                      "transactionId": "TXN-1002",
                      "date": "2026-09-02",
                      "customerId": "CUST-002",
                      "amount": -500.00,
                      "taxRate": 18.00,
                      "reportedTax": 0.00,
                      "transactionType": "SALE"
                    }
                  ]
                }
                """);

        assertEquals(201, responseEntity.statusCode());
        BatchUploadResponse response = objectMapper.readValue(responseEntity.body(), BatchUploadResponse.class);
        assertEquals("PARTIAL_SUCCESS", response.status().name());
        assertEquals(2, response.totalTransactions());
        assertEquals(1, response.successfulTransactions());
        assertEquals(1, response.failedTransactions());

        assertEquals(1, transactionRepository.countByBatchIdAndValidationStatus(response.batchId(), ValidationStatus.SUCCESS));
        assertEquals(1, transactionRepository.countByBatchIdAndValidationStatus(response.batchId(), ValidationStatus.FAILURE));
        assertTrue(response.transactions().get(1).failureReasons().contains("amount must be greater than 0"));
    }

    @Test
    void uploadBatchContainingInvalidDatePersistsFailureAndRawJson() throws Exception {
        HttpResponse<String> responseEntity = postBatch("""
                {
                  "transactions": [
                    {
                      "transactionId": "TXN-1003",
                      "date": "2026/09/05",
                      "customerId": "CUST-003",
                      "amount": 500.00,
                      "taxRate": 18.00,
                      "reportedTax": 90.00,
                      "transactionType": "EXPENSE"
                    }
                  ]
                }
                """);

        assertEquals(201, responseEntity.statusCode());
        BatchUploadResponse response = objectMapper.readValue(responseEntity.body(), BatchUploadResponse.class);
        assertEquals("FAILURE", response.status().name());
        assertEquals(1, response.failedTransactions());
        Transaction transaction = transactionRepository.findByBatchId(response.batchId()).get(0);

        assertEquals(ValidationStatus.FAILURE, transaction.getValidationStatus());
        assertTrue(transaction.getFailureReasons().contains("date must be valid in yyyy-MM-dd format"));
        assertEquals("2026/09/05", transaction.getRawTransaction().get("date"));
    }

    @Test
    void malformedRequestReturnsStructuredApiError() throws Exception {
        HttpResponse<String> responseEntity = postBatch("{ invalid-json");

        assertEquals(400, responseEntity.statusCode());
        assertTrue(responseEntity.body().contains("\"errorCode\":\"MALFORMED_REQUEST\""));
        assertTrue(responseEntity.body().contains("\"path\":\"/api/v1/transactions/batch\""));
    }

    @Test
    void validAndInvalidTransactionsCanCoexistInSameBatch() throws Exception {
        HttpResponse<String> responseEntity = postBatch("""
                {
                  "transactions": [
                    {
                      "transactionId": "TXN-1004",
                      "date": "2026-09-05",
                      "customerId": "CUST-004",
                      "amount": 250.00,
                      "taxRate": 18.00,
                      "reportedTax": 45.00,
                      "transactionType": "REFUND"
                    },
                    {
                      "transactionId": "",
                      "date": "abc",
                      "customerId": "",
                      "amount": 0,
                      "taxRate": 18.00,
                      "reportedTax": 0.00,
                      "transactionType": "INVALID"
                    }
                  ]
                }
                """);

        assertEquals(201, responseEntity.statusCode());
        BatchUploadResponse response = objectMapper.readValue(responseEntity.body(), BatchUploadResponse.class);
        List<Transaction> transactions = transactionRepository.findByBatchId(response.batchId());

        assertEquals(2, transactions.size());
        assertFalse(transactions.stream().filter(transaction -> transaction.getValidationStatus() == ValidationStatus.SUCCESS).toList().isEmpty());
        assertFalse(transactions.stream().filter(transaction -> transaction.getValidationStatus() == ValidationStatus.FAILURE).toList().isEmpty());
    }

    private HttpResponse<String> postBatch(String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/transactions/batch"))
                .header("Authorization", AUTHORIZATION_HEADER)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
