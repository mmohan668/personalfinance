package com.pf.common.dto.financialTransaction;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialTransactionDto {

    private Long id;

    private BigDecimal amount;

    private LocalDate transactionAt;

    private Long transactionTypeId;

    private Long categoryId;

    private Long subcategoryId;

    private Long locationId;

    private String remarks;

    private Long adminUserId;

    private Long createdBy;

    private LocalDateTime createdAt;

    private Long updatedBy;

    private LocalDateTime updatedAt;
}
