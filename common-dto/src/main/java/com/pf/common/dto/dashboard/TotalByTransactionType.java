package com.pf.common.dto.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TotalByTransactionType {
    private BigDecimal expense = BigDecimal.ZERO;
    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal investment = BigDecimal.ZERO;
    private BigDecimal transfer = BigDecimal.ZERO;
}
