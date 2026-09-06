package com.taxcompliance.dto.response;

import com.taxcompliance.enums.ExceptionSeverity;
import com.taxcompliance.enums.RuleType;

import java.util.Map;
import java.util.UUID;

public record RuleExceptionResponse(
        UUID exceptionId,
        UUID transactionId,
        String externalTransactionId,
        String ruleCode,
        RuleType ruleType,
        ExceptionSeverity severity,
        String message,
        Map<String, Object> details) {
}
