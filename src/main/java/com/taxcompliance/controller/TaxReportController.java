package com.taxcompliance.controller;

import com.taxcompliance.dto.response.CustomerTaxSummaryResponse;
import com.taxcompliance.service.TaxReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(
        name = "Tax Reports",
        description = "Tax compliance reporting APIs"
)
public class TaxReportController {

    private final TaxReportService taxReportService;

    public TaxReportController(
            TaxReportService taxReportService) {

        this.taxReportService = taxReportService;
    }

    @GetMapping("/customers/tax-summary")
    @Operation(
            summary = "Get customer tax summary",
            description = """
                    Returns aggregated tax information for each customer,
                    including total amount, reported tax, expected tax,
                    tax gap and compliance score.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer tax summary generated successfully"
            )
    })
    public ResponseEntity<List<CustomerTaxSummaryResponse>>
    getCustomerTaxSummary() {

        return ResponseEntity.ok(
                taxReportService.getCustomerTaxSummary()
        );
    }
}