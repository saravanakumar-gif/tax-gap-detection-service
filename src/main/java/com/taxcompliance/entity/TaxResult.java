package com.taxcompliance.entity;

import com.taxcompliance.enums.ComplianceStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "tax_result",
        indexes = {
                @Index(
                        name = "idx_tax_result_transaction_id",
                        columnList = "transaction_id"
                ),
                @Index(
                        name = "idx_tax_result_compliance_status",
                        columnList = "compliance_status"
                )
        }
)
public class TaxResult {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "transaction_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_tax_result_transaction"
            )
    )
    private Transaction transaction;

    @Column(
            name = "expected_tax",
            precision = 19,
            scale = 4
    )
    private BigDecimal expectedTax;

    @Column(
            name = "tax_gap",
            precision = 19,
            scale = 4
    )
    private BigDecimal taxGap;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "compliance_status",
            nullable = false,
            length = 30
    )
    private ComplianceStatus complianceStatus;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    protected TaxResult() {
    }

    public TaxResult(
            Transaction transaction,
            BigDecimal expectedTax,
            BigDecimal taxGap,
            ComplianceStatus complianceStatus,
            Instant calculatedAt) {

        this.transaction = transaction;
        this.expectedTax = expectedTax;
        this.taxGap = taxGap;
        this.complianceStatus = complianceStatus;
        this.calculatedAt = calculatedAt;
    }

    public UUID getId() {
        return id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public BigDecimal getExpectedTax() {
        return expectedTax;
    }

    public BigDecimal getTaxGap() {
        return taxGap;
    }

    public ComplianceStatus getComplianceStatus() {
        return complianceStatus;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    public void updateCalculation(
            BigDecimal expectedTax,
            BigDecimal taxGap,
            ComplianceStatus complianceStatus,
            Instant calculatedAt) {

        this.expectedTax = expectedTax;
        this.taxGap = taxGap;
        this.complianceStatus = complianceStatus;
        this.calculatedAt = calculatedAt;
    }
}