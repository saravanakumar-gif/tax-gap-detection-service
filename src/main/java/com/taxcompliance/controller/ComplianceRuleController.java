package com.taxcompliance.controller;

import com.taxcompliance.dto.response.ComplianceRuleExecutionResponse;
import com.taxcompliance.service.ComplianceRuleEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compliance/rules")
public class ComplianceRuleController {

    private final ComplianceRuleEngineService complianceRuleEngineService;

    public ComplianceRuleController(ComplianceRuleEngineService complianceRuleEngineService) {
        this.complianceRuleEngineService = complianceRuleEngineService;
    }

    @PostMapping("/execute/batch/{batchId}")
    public ResponseEntity<ComplianceRuleExecutionResponse> executeForBatch(@PathVariable UUID batchId) {
        return ResponseEntity.ok(complianceRuleEngineService.executeForBatch(batchId));
    }
}
