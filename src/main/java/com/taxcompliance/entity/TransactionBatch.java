package com.taxcompliance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transaction_batches")
public class TransactionBatch {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "batch_reference", nullable = false, unique = true, length = 64)
    private String batchReference;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Column(name = "total_transactions", nullable = false)
    private int totalTransactions;

    @Column(name = "successful_transactions", nullable = false)
    private int successfulTransactions;

    @Column(name = "failed_transactions", nullable = false)
    private int failedTransactions;

    protected TransactionBatch() {
    }

    public TransactionBatch(String batchReference, Instant uploadedAt) {
        this.batchReference = batchReference;
        this.uploadedAt = uploadedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getBatchReference() {
        return batchReference;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public int getSuccessfulTransactions() {
        return successfulTransactions;
    }

    public int getFailedTransactions() {
        return failedTransactions;
    }

    public void updateSummary(int totalTransactions, int successfulTransactions, int failedTransactions) {
        this.totalTransactions = totalTransactions;
        this.successfulTransactions = successfulTransactions;
        this.failedTransactions = failedTransactions;
    }
}
