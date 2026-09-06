package com.taxcompliance.service;

import com.taxcompliance.enums.ExceptionSeverity;

import java.util.Map;

record RuleEvaluationResult(
        boolean violation,
        ExceptionSeverity severity,
        String message,
        Map<String, Object> details) {

    static RuleEvaluationResult noViolation() {
        return new RuleEvaluationResult(false, null, null, Map.of());
    }

    static RuleEvaluationResult violation(
            ExceptionSeverity severity,
            String message,
            Map<String, Object> details) {
        return new RuleEvaluationResult(true, severity, message, details);
    }
}
