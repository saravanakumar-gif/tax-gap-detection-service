package com.taxcompliance.entity;

import com.taxcompliance.enums.AuditEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private AuditEventType eventType;

    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId;

    @Column(name = "event_timestamp", nullable = false)
    private Instant timestamp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detail_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> detailJson;

    protected AuditLog() {
    }

    public AuditLog(
            AuditEventType eventType,
            String transactionId,
            Instant timestamp,
            Map<String, Object> detailJson) {

        this.eventType = eventType;
        this.transactionId = transactionId;
        this.timestamp = timestamp;
        this.detailJson = detailJson;
    }

    public UUID getId() {
        return id;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getDetailJson() {
        return detailJson;
    }
}