package com.taxcompliance.repository;

import com.taxcompliance.entity.ExceptionRecord;
import com.taxcompliance.repository.projection.CustomerExceptionCount;
import com.taxcompliance.repository.projection.SeverityExceptionCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExceptionRecordRepository extends JpaRepository<ExceptionRecord, UUID> {

    List<ExceptionRecord> findByTransactionBatchId(UUID batchId);

//    void deleteByTransactionBatchId(UUID batchId);
    /*
     * Delete all exceptions belonging to a batch.
     * This is used when the compliance rules are executed again
     * for the same batch.
     */
    @Modifying
    @Query("""
            DELETE FROM ExceptionRecord e
            WHERE e.transaction.batch.id = :batchId
            """)
    void deleteByTransactionBatchId(
            @Param("batchId") UUID batchId
    );


    /*
     * Count exceptions grouped by severity.
     *
     * Executed directly in the database.
     */
    @Query("""
            SELECT
                e.severity AS severity,
                COUNT(e.id) AS exceptionCount
            FROM ExceptionRecord e
            GROUP BY e.severity
            ORDER BY e.severity
            """)
    List<SeverityExceptionCount> countBySeverity();


    /*
     * Count exceptions grouped by customer.
     *
     * Customer ID is obtained from the related Transaction.
     *
     * Executed directly in the database.
     */
    @Query("""
            SELECT
                t.customerId AS customerId,
                COUNT(e.id) AS exceptionCount
            FROM ExceptionRecord e
            JOIN e.transaction t
            GROUP BY t.customerId
            ORDER BY t.customerId
            """)
    List<CustomerExceptionCount> countByCustomer();

}
