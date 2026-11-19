package com.pf.common.repository.bulkUpload;

import com.pf.common.entity.bulkUpload.InvoiceUploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

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

    @Query("""
            SELECT invoiceStatus FROM InvoiceUploadStatus invoiceStatus
            WHERE invoiceStatus.id IN :ids
              AND invoiceStatus.uploadedBy.adminUser.id = :adminUserId
            """)
    List<InvoiceUploadStatus> findAllByIdAndAdminUserId(
            @Param("ids") Collection<Long> ids,
            @Param("adminUserId") Long adminUserId
    );

    @Modifying
    @Query("DELETE FROM InvoiceUploadStatus invoiceStatus WHERE invoiceStatus.uploadBatchId IN :batchIds")
    int deleteAllByUploadBatchIdIn(@Param("batchIds") Collection<UUID> batchIds);
}
