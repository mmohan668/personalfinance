package com.pf.common.repository.bulkUpload;

import com.pf.common.entity.bulkUpload.InvoiceUploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface InvoiceUploadStatusRepository extends JpaRepository<InvoiceUploadStatus, Long> {
    @Query("""
            SELECT invoiceStatus
            FROM InvoiceUploadStatus invoiceStatus
            WHERE LOWER(TRIM(invoiceStatus.invoiceNumber)) IN :invoiceNumbers
              AND invoiceStatus.status = :status
            ORDER BY invoiceStatus.id
            """)
    List<InvoiceUploadStatus> findByNormalizedInvoiceNumbersAndStatus(
            @Param("invoiceNumbers") Collection<String> invoiceNumbers,
            @Param("status") String status
    );
}
