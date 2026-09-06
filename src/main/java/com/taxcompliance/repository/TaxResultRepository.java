package com.taxcompliance.repository;

import com.taxcompliance.entity.TaxResult;
import com.taxcompliance.enums.ComplianceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxResultRepository
        extends JpaRepository<TaxResult, UUID> {

    Optional<TaxResult> findByTransactionId(UUID transactionId);

    List<TaxResult> findByComplianceStatus(
            ComplianceStatus complianceStatus
    );
}