package com.taxcompliance.entity;

import com.taxcompliance.enums.ExceptionSeverity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "exception_records",
        indexes = {
                @Index(
                        name = "idx_exception_transaction_id",
                        columnList = "transaction_id"
                ),
                @Index(
                        name = "idx_exception_severity",
                        columnList = "severity"
                )
        }
)
public class ExceptionRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "transaction_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_exception_transaction"
            )
    )
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "rule_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_exception_rule"
            )
    )
    private ComplianceRule rule;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 60)
    private com.taxcompliance.enums.RuleType ruleType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "severity",
            nullable = false,
            length = 20
    )
    private ExceptionSeverity severity;

    @Column(
            name = "message",
            nullable = false,
            length = 1000
    )
    private String message;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "details", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> details;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    protected ExceptionRecord() {
    }

    public ExceptionRecord(
            Transaction transaction,
            ComplianceRule rule,
            ExceptionSeverity severity,
            String message,
            Map<String, Object> details,
            Instant createdAt) {

        this.transaction = transaction;
        this.rule = rule;
        this.ruleCode = rule.getRuleCode();
        this.ruleType = rule.getRuleType();
        this.severity = severity;
        this.message = message;
        this.details = details != null ? Map.copyOf(details) : Map.of();
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public ComplianceRule getRule() {
        return rule;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public com.taxcompliance.enums.RuleType getRuleType() {
        return ruleType;
    }

    public ExceptionSeverity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
