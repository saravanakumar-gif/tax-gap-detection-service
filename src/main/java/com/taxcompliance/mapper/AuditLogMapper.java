package com.taxcompliance.mapper;

import com.taxcompliance.dto.audit.AuditLogResponse;
import com.taxcompliance.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog auditLog) {

        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getEventType(),
                auditLog.getTransactionId(),
                auditLog.getTimestamp(),
                auditLog.getDetailJson()
        );
    }
}