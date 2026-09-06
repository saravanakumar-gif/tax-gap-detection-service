package com.taxcompliance.dto.response;

import java.util.List;

public record ExceptionSummaryReportResponse(

        long totalExceptions,

        List<SeverityExceptionSummary> countBySeverity,

        List<CustomerExceptionSummary> customerWiseExceptionCount

) {
}