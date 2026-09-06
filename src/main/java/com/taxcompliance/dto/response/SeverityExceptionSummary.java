package com.taxcompliance.dto.response;

public record SeverityExceptionSummary(
        String severity,
        Long count
) {
}