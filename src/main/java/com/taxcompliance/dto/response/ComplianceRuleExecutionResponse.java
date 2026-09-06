package com.taxcompliance.dto.response;

import java.util.List;
import java.util.UUID;

public record ComplianceRuleExecutionResponse(
        UUID batchId,
        int evaluatedTransactions,
        int activeRules,
        int exceptionsCreated,
        List<RuleExceptionResponse> exceptions) {
}
