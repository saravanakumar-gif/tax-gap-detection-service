package com.taxcompliance.controller;

import com.taxcompliance.dto.response.BatchUploadResponse;
import com.taxcompliance.dto.response.ComplianceRuleExecutionResponse;
import com.taxcompliance.config.TestSecurityConfig;
import com.taxcompliance.repository.ExceptionRecordRepository;
import com.taxcompliance.repository.TaxResultRepository;
import com.taxcompliance.repository.TransactionBatchRepository;
import com.taxcompliance.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
class ComplianceRuleControllerIntegrationTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AUTHORIZATION_HEADER = "Basic "
            + Base64.getEncoder().encodeToString("auditor:password".getBytes(StandardCharsets.UTF_8));
    private static final String PASSWORD_HASH =
            "$2a$10$GM/fV7dS.FI.MulbjcEFu.7kYo3sWHMlARx2eOib0YZIgW77SXqLq";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ExceptionRecordRepository exceptionRecordRepository;

    @Autowired
    private TaxResultRepository taxResultRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionBatchRepository transactionBatchRepository;

    @BeforeEach
    void cleanDatabase() {
        seedAuditorUser();
        exceptionRecordRepository.deleteAll();
        taxResultRepository.deleteAll();
        transactionRepository.deleteAll();
        transactionBatchRepository.deleteAll();
        jdbcTemplate.update("UPDATE compliance_rules SET enabled = true");
    }

    @AfterEach
    void restoreRules() {
        jdbcTemplate.update("UPDATE compliance_rules SET enabled = true");
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
    void executeActiveRulesCreatesExceptionsForMandatoryRuleViolations() throws Exception {
        BatchUploadResponse batch = uploadBatch("""
                {
                  "transactions": [
                    {
                      "transactionId": "SALE-001",
                      "date": "2026-09-01",
                      "customerId": "CUST-001",
                      "amount": 1000.00,
                      "taxRate": 18.00,
                      "reportedTax": 18000.00,
                      "transactionType": "SALE"
                    },
                    {
                      "transactionId": "HIGH-001",
                      "date": "2026-09-01",
                      "customerId": "CUST-002",
                      "amount": 150000.00,
                      "taxRate": 18.00,
                      "reportedTax": 2700000.00,
                      "transactionType": "SALE"
                    },
                    {
                      "transactionId": "REFUND-001",
                      "date": "2026-09-02",
                      "customerId": "CUST-001",
                      "amount": 1500.00,
                      "taxRate": 18.00,
                      "reportedTax": 27000.00,
                      "transactionType": "REFUND",
                      "originalTransactionId": "SALE-001"
                    },
                    {
                      "transactionId": "GST-001",
                      "date": "2026-09-03",
                      "customerId": "CUST-003",
                      "amount": 2000.00,
                      "taxRate": 12.00,
                      "reportedTax": 24000.00,
                      "transactionType": "SALE"
                    }
                  ]
                }
                """);

        ComplianceRuleExecutionResponse response = executeRules(batch);

        assertEquals(4, response.evaluatedTransactions());
        assertEquals(3, response.activeRules());
        assertEquals(3, response.exceptionsCreated());
        assertEquals(3, exceptionRecordRepository.findByTransactionBatchId(batch.batchId()).size());
        assertTrue(response.exceptions().stream().anyMatch(exception -> exception.ruleCode().equals("HIGH_VALUE_TRANSACTION")));
        assertTrue(response.exceptions().stream().anyMatch(exception -> exception.ruleCode().equals("REFUND_VALIDATION")));
        assertTrue(response.exceptions().stream().anyMatch(exception -> exception.ruleCode().equals("GST_SLAB_VIOLATION")));
    }

    @Test
    void disabledRulesAreNotExecuted() throws Exception {
        jdbcTemplate.update("UPDATE compliance_rules SET enabled = false WHERE rule_code = 'HIGH_VALUE_TRANSACTION'");
        BatchUploadResponse batch = uploadBatch("""
                {
                  "transactions": [
                    {
                      "transactionId": "HIGH-002",
                      "date": "2026-09-01",
                      "customerId": "CUST-002",
                      "amount": 150000.00,
                      "taxRate": 18.00,
                      "reportedTax": 2700000.00,
                      "transactionType": "SALE"
                    }
                  ]
                }
                """);

        ComplianceRuleExecutionResponse response = executeRules(batch);

        assertEquals(1, response.evaluatedTransactions());
        assertEquals(2, response.activeRules());
        assertEquals(0, response.exceptionsCreated());
    }

    private BatchUploadResponse uploadBatch(String body) throws Exception {
        HttpResponse<String> response = sendPost("/api/v1/transactions/batch", body);
        assertEquals(201, response.statusCode());
        return objectMapper.readValue(response.body(), BatchUploadResponse.class);
    }

    private ComplianceRuleExecutionResponse executeRules(BatchUploadResponse batch) throws Exception {
        HttpResponse<String> response = sendPost("/api/v1/compliance/rules/execute/batch/" + batch.batchId(), "");
        assertEquals(200, response.statusCode());
        return objectMapper.readValue(response.body(), ComplianceRuleExecutionResponse.class);
    }

    private HttpResponse<String> sendPost(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Authorization", AUTHORIZATION_HEADER)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
