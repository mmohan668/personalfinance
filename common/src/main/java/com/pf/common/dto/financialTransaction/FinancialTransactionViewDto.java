package com.pf.common.dto.financialTransaction;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FinancialTransactionViewDto {

    private Long id;

    private LocalDate transactionAt;

    private BigDecimal amount;

    private Long transactionTypeId;

    private String transactionType;

    private Long categoryId;

    private String categoryName;

    private String categoryDescription;

    private Long subcategoryId;

    private String subcategoryName;

    private String subcategoryDescription;

    private String remarks;

    private Long locationId;

    private String location;

    private String locationDescription;

    private Long adminUserId;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;
}
