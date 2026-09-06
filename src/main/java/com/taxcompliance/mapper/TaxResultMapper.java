package com.taxcompliance.mapper;

import com.taxcompliance.dto.response.TaxResultResponse;
import com.taxcompliance.entity.TaxResult;
import org.springframework.stereotype.Component;

@Component
public class TaxResultMapper {

    public TaxResultResponse toResponse(TaxResult taxResult) {

        return new TaxResultResponse(
                taxResult.getId(),
                taxResult.getTransaction().getId(),
                taxResult.getTransaction().getTransactionId(),
                taxResult.getExpectedTax(),
                taxResult.getTransaction().getReportedTax(),
                taxResult.getTaxGap(),
                taxResult.getComplianceStatus(),
                taxResult.getCalculatedAt()
        );
    }
}