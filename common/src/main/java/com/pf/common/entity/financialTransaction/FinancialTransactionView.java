package com.pf.common.entity.financialTransaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Immutable
@Table(name = "v_financial_transactions")
public class FinancialTransactionView {

    @Id
    private Long id;

    @Column(name = "transaction_at")
    private LocalDateTime transactionAt;

    private BigDecimal amount;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "category_description")
    private String categoryDescription;

    @Column(name = "subcategory_name")
    private String subcategoryName;

    @Column(name = "subcategory_description")
    private String subcategoryDescription;

    private String remarks;

    private String location;

    @Column(name = "location_description")
    private String locationDescription;

    @Column(name = "admin_user_id")
    private Long adminUserId;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
