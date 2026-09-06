package com.taxcompliance.mapper;

import com.taxcompliance.dto.response.RuleExceptionResponse;
import com.taxcompliance.entity.ExceptionRecord;
import org.springframework.stereotype.Component;

@Component
public class ExceptionRecordMapper {

    public RuleExceptionResponse toResponse(ExceptionRecord exceptionRecord) {
        return new RuleExceptionResponse(
                exceptionRecord.getId(),
                exceptionRecord.getTransaction().getId(),
                exceptionRecord.getTransaction().getTransactionId(),
                exceptionRecord.getRuleCode(),
                exceptionRecord.getRuleType(),
                exceptionRecord.getSeverity(),
                exceptionRecord.getMessage(),
                exceptionRecord.getDetails());
    }
}
