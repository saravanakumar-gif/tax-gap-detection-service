package com.taxcompliance.service;

import com.taxcompliance.dto.response.CustomerExceptionSummary;
import com.taxcompliance.dto.response.ExceptionSummaryReportResponse;
import com.taxcompliance.dto.response.SeverityExceptionSummary;
import com.taxcompliance.repository.ExceptionRecordRepository;
import com.taxcompliance.repository.projection.CustomerExceptionCount;
import com.taxcompliance.repository.projection.SeverityExceptionCount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExceptionSummaryReportService {

    private final ExceptionRecordRepository exceptionRecordRepository;

    public ExceptionSummaryReportService(
            ExceptionRecordRepository exceptionRecordRepository) {

        this.exceptionRecordRepository = exceptionRecordRepository;
    }

    @Transactional(readOnly = true)
    public ExceptionSummaryReportResponse getExceptionSummary() {

        /*
         * Query 1:
         * Count total exceptions directly in the database.
         */
        long totalExceptions = exceptionRecordRepository.count();

        /*
         * Query 2:
         * Database performs:
         *
         * SELECT severity, COUNT(id)
         * FROM exception_records
         * GROUP BY severity
         */
        List<SeverityExceptionCount> severityResults =
                exceptionRecordRepository.countBySeverity();

        /*
         * Convert repository projection
         * into API response DTO.
         */
        List<SeverityExceptionSummary> countBySeverity =
                severityResults.stream()
                        .map(result ->
                                new SeverityExceptionSummary(
                                        result.getSeverity().name(),
                                        result.getExceptionCount()
                                )
                        )
                        .toList();

        /*
         * Query 3:
         * Database performs:
         *
         * SELECT customer_id, COUNT(id)
         * FROM exception_records
         * GROUP BY customer_id
         */
        List<CustomerExceptionCount> customerResults =
                exceptionRecordRepository.countByCustomer();

        /*
         * Convert repository projection
         * into API response DTO.
         */
        List<CustomerExceptionSummary> customerWiseExceptionCount =
                customerResults.stream()
                        .map(result ->
                                new CustomerExceptionSummary(
                                        result.getCustomerId(),
                                        result.getExceptionCount()
                                )
                        )
                        .toList();

        /*
         * Return final report.
         */
        return new ExceptionSummaryReportResponse(
                totalExceptions,
                countBySeverity,
                customerWiseExceptionCount
        );
    }
}