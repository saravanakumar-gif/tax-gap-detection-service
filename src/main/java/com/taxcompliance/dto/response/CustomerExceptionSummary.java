package com.taxcompliance.dto.response;

public record CustomerExceptionSummary(
        String customerId,
        Long count
) {
}