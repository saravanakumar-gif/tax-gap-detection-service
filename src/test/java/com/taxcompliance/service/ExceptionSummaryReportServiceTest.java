package com.taxcompliance.service;

import com.taxcompliance.dto.response.ExceptionSummaryReportResponse;
import com.taxcompliance.enums.ExceptionSeverity;
import com.taxcompliance.repository.ExceptionRecordRepository;
import com.taxcompliance.repository.projection.CustomerExceptionCount;
import com.taxcompliance.repository.projection.SeverityExceptionCount;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExceptionSummaryReportServiceTest {

    private final ExceptionRecordRepository exceptionRecordRepository =
            mock(ExceptionRecordRepository.class);

    private final ExceptionSummaryReportService service =
            new ExceptionSummaryReportService(exceptionRecordRepository);

    @Test
    void getExceptionSummaryUsesDatabaseAggregateResults() {
        when(exceptionRecordRepository.count()).thenReturn(4L);
        when(exceptionRecordRepository.countBySeverity()).thenReturn(List.of(
                severityCount(ExceptionSeverity.HIGH, 3L),
                severityCount(ExceptionSeverity.MEDIUM, 1L)
        ));
        when(exceptionRecordRepository.countByCustomer()).thenReturn(List.of(
                customerCount("CUST-001", 2L),
                customerCount("CUST-002", 2L)
        ));

        ExceptionSummaryReportResponse response = service.getExceptionSummary();

        assertEquals(4L, response.totalExceptions());
        assertEquals(2, response.countBySeverity().size());
        assertEquals("HIGH", response.countBySeverity().get(0).severity());
        assertEquals(3L, response.countBySeverity().get(0).count());
        assertEquals("MEDIUM", response.countBySeverity().get(1).severity());
        assertEquals(1L, response.countBySeverity().get(1).count());
        assertEquals(2, response.customerWiseExceptionCount().size());
        assertEquals("CUST-001", response.customerWiseExceptionCount().get(0).customerId());
        assertEquals(2L, response.customerWiseExceptionCount().get(0).count());
        assertEquals("CUST-002", response.customerWiseExceptionCount().get(1).customerId());
        assertEquals(2L, response.customerWiseExceptionCount().get(1).count());

        verify(exceptionRecordRepository).count();
        verify(exceptionRecordRepository).countBySeverity();
        verify(exceptionRecordRepository).countByCustomer();
    }

    private SeverityExceptionCount severityCount(
            ExceptionSeverity severity,
            Long exceptionCount) {

        return new SeverityExceptionCount() {
            @Override
            public ExceptionSeverity getSeverity() {
                return severity;
            }

            @Override
            public Long getExceptionCount() {
                return exceptionCount;
            }
        };
    }

    private CustomerExceptionCount customerCount(
            String customerId,
            Long exceptionCount) {

        return new CustomerExceptionCount() {
            @Override
            public String getCustomerId() {
                return customerId;
            }

            @Override
            public Long getExceptionCount() {
                return exceptionCount;
            }
        };
    }
}
