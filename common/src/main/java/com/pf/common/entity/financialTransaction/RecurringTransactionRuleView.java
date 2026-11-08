package com.pf.common.entity.financialTransaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "vw_recurring_transaction_rules")
@Getter
@Setter
@NoArgsConstructor
public class RecurringTransactionRuleView {
    @Id
    private Long id;

    @Column(name = "admin_user_id")
    private Long adminUserId;

    @Column(name = "created_by_id")
    private Long createdById;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "rule_name")
    private String ruleName;

    @Column(name = "subcategory_id")
    private Long subcategoryId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "location_id")
    private Long locationId;

    private String location;

    private String transactionType;
    private String categoryName;
    private String subcategoryName;
    private BigDecimal amount;
    private String frequency;
    private String remarks;
    private Integer intervalValue;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    private Integer monthOfYear;
    private String monthlyPattern;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime executionTime;

    @Column(name = "is_active")
    private Boolean active;
}
