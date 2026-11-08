package com.pf.common.dto.recurringTransaction;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringTransactionRuleDto {
    private Long id;
    private Long createdById;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String ruleName;
    private Long subcategoryId;
    private Long categoryId;
    private String transactionType;
    private String categoryName;
    private String subcategoryName;
    private Long locationId;
    private BigDecimal amount;
    private String remarks;
    private String frequency;
    private Integer intervalValue;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    private Integer monthOfYear;
    private String monthlyPattern;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime executionTime;
    private Boolean active;
}
