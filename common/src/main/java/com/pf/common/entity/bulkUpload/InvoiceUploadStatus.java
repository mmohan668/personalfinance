package com.pf.common.entity.bulkUpload;

import com.pf.common.entity.userManagement.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoice_upload_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceUploadStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "status", nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "category_subcategory_suggestion", columnDefinition = "jsonb")
    private String categorySubcategorySuggestion;

    @Column(name = "upload_batch_id", columnDefinition = "uuid")
    private UUID uploadBatchId;

    @Column(name = "uploaded_date", nullable = false)
    private LocalDate uploadedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (uploadedDate == null) uploadedDate = LocalDate.now();
        if (uploadedAt == null) uploadedAt = LocalDateTime.now();
    }
}
