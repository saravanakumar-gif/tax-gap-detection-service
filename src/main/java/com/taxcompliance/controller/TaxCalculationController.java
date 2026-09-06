package com.taxcompliance.controller;

import com.taxcompliance.dto.response.TaxCalculationBatchResponse;
import com.taxcompliance.service.TaxCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tax")
public class TaxCalculationController {

    private final TaxCalculationService taxCalculationService;

    public TaxCalculationController(
            TaxCalculationService taxCalculationService) {

        this.taxCalculationService = taxCalculationService;
    }

    @PostMapping("/calculate/batch/{batchId}")
    public ResponseEntity<TaxCalculationBatchResponse>
    calculateBatch(
            @PathVariable UUID batchId) {

        TaxCalculationBatchResponse response =
                taxCalculationService.calculateForBatch(
                        batchId
                );

        return ResponseEntity.ok(response);
    }
}