package com.taxcompliance.controller;

import com.taxcompliance.dto.response.ExceptionSummaryReportResponse;
import com.taxcompliance.service.ReportingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/exceptions/summary")
    public ResponseEntity<ExceptionSummaryReportResponse>
    getExceptionSummaryReport() {

        return ResponseEntity.ok(
                reportingService.getExceptionSummaryReport()
        );
    }
}