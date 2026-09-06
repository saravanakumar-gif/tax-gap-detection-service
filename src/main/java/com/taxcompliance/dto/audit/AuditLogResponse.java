package com.taxcompliance.dto.audit;

import com.taxcompliance.enums.AuditEventType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        AuditEventType eventType,
        String transactionId,
        Instant timestamp,
        Map<String, Object> detailJson
) {
}