package com.taxcompliance.repository;

import com.taxcompliance.entity.AuditLog;
import com.taxcompliance.enums.AuditEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByTransactionIdOrderByTimestampAsc(String transactionId);

    List<AuditLog> findByEventTypeOrderByTimestampDesc(
            AuditEventType eventType);
}