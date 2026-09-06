package com.taxcompliance.service;

import com.taxcompliance.dto.audit.AuditLogResponse;
import com.taxcompliance.entity.AuditLog;
import com.taxcompliance.enums.AuditEventType;
import com.taxcompliance.mapper.AuditLogMapper;
import com.taxcompliance.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            AuditLogMapper auditLogMapper) {

        this.auditLogRepository = auditLogRepository;
        this.auditLogMapper = auditLogMapper;
    }

    @Transactional
    public AuditLogResponse record(
            AuditEventType eventType,
            String transactionId,
            Map<String, Object> detailJson) {

        AuditLog auditLog = new AuditLog(
                eventType,
                transactionId,
                Instant.now(),
                detailJson
        );

        AuditLog savedAuditLog = auditLogRepository.save(auditLog);

        return auditLogMapper.toResponse(savedAuditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByTransactionId(
            String transactionId) {

        return auditLogRepository
                .findByTransactionIdOrderByTimestampAsc(transactionId)
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByEventType(
            AuditEventType eventType) {

        return auditLogRepository
                .findByEventTypeOrderByTimestampDesc(eventType)
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }
}