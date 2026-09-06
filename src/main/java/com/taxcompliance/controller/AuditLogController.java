package com.taxcompliance.controller;

import com.taxcompliance.dto.audit.AuditLogResponse;
import com.taxcompliance.enums.AuditEventType;
import com.taxcompliance.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(
        name = "Audit Logs",
        description = "APIs for retrieving transaction audit history"
)
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/transaction/{transactionId}")
    @Operation(
            summary = "Get audit history for a transaction"
    )
    public ResponseEntity<List<AuditLogResponse>> getByTransactionId(
            @Parameter(
                    description = "Business transaction ID",
                    example = "TXN-1001"
            )
            @PathVariable String transactionId) {

        return ResponseEntity.ok(
                auditLogService.getByTransactionId(transactionId)
        );
    }

    @GetMapping
    @Operation(
            summary = "Get audit logs by event type"
    )
    public ResponseEntity<List<AuditLogResponse>> getByEventType(
            @RequestParam AuditEventType eventType) {

        return ResponseEntity.ok(
                auditLogService.getByEventType(eventType)
        );
    }
}