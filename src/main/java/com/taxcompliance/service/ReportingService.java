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
public class ReportingService {

    private final ExceptionRecordRepository exceptionRecordRepository;

    public ReportingService(
            ExceptionRecordRepository exceptionRecordRepository) {
        this.exceptionRecordRepository = exceptionRecordRepository;
    }

    @Transactional(readOnly = true)
    public ExceptionSummaryReportResponse getExceptionSummaryReport() {

        long totalExceptions = exceptionRecordRepository.count();

        List<SeverityExceptionSummary> countBySeverity =
                exceptionRecordRepository.countBySeverity()
                        .stream()
                        .map(this::toSeveritySummary)
                        .toList();

        List<CustomerExceptionSummary> customerWiseExceptionCount =
                exceptionRecordRepository.countByCustomer()
                        .stream()
                        .map(this::toCustomerSummary)
                        .toList();

        return new ExceptionSummaryReportResponse(
                totalExceptions,
                countBySeverity,
                customerWiseExceptionCount
        );
    }

    private SeverityExceptionSummary toSeveritySummary(
            SeverityExceptionCount projection) {

        return new SeverityExceptionSummary(
                projection.getSeverity().name(),
                projection.getExceptionCount()
        );
    }

    private CustomerExceptionSummary toCustomerSummary(
            CustomerExceptionCount projection) {

        return new CustomerExceptionSummary(
                projection.getCustomerId(),
                projection.getExceptionCount()
        );
    }
}
