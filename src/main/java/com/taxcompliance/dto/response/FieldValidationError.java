package com.taxcompliance.dto.response;

public record FieldValidationError(
        String field,
        String message) {
}
