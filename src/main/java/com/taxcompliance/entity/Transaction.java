package com.taxcompliance.entity;

import com.taxcompliance.enums.TransactionType;
import com.taxcompliance.enums.ValidationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "financial_transactions")
public class Transaction {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private TransactionBatch batch;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "transaction_date")
    private LocalDate date;

    @Column(name = "customer_id", length = 100)
    private String customerId;

    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "tax_rate", precision = 9, scale = 4)
    private BigDecimal taxRate;

    @Column(name = "reported_tax", precision = 19, scale = 4)
    private BigDecimal reportedTax;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false, length = 20)
    private ValidationStatus validationStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "failure_reasons", nullable = false, columnDefinition = "jsonb")
    private List<String> failureReasons;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_transaction", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> rawTransaction;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Transaction() {
    }

    public Transaction(
            TransactionBatch batch,
            String transactionId,
            LocalDate date,
            String customerId,
            BigDecimal amount,
            BigDecimal taxRate,
            BigDecimal reportedTax,
            TransactionType transactionType,
            ValidationStatus validationStatus,
            List<String> failureReasons,
            Map<String, Object> rawTransaction,
            Instant createdAt) {
        this.batch = batch;
        this.transactionId = transactionId;
        this.date = date;
        this.customerId = customerId;
        this.amount = amount;
        this.taxRate = taxRate;
        this.reportedTax = reportedTax;
        this.transactionType = transactionType;
        this.validationStatus = validationStatus;
        this.failureReasons = failureReasons;
        this.rawTransaction = rawTransaction;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public TransactionBatch getBatch() {
        return batch;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public BigDecimal getReportedTax() {
        return reportedTax;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public List<String> getFailureReasons() {
        return failureReasons;
    }

    public Map<String, Object> getRawTransaction() {
        return rawTransaction;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
