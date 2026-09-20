package com.pf.common.entity.financialTransaction;

import com.pf.common.entity.categoryManagement.UserCategory;
import com.pf.common.entity.categoryManagement.UserSubcategory;
import com.pf.common.entity.settings.ReferenceValue;
import com.pf.common.entity.userManagement.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_at", nullable = false)
    private LocalDate transactionAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_type_id", nullable = false)
    private ReferenceValue transactionType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private UserCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subcategory_id", nullable = false)
    private UserSubcategory subcategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private ReferenceValue location;

    @Column(name = "remarks", length = 4000)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private User adminUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
}
