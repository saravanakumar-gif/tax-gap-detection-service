package com.taxcompliance.repository;

import com.taxcompliance.entity.TransactionBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionBatchRepository extends JpaRepository<TransactionBatch, UUID> {
}
