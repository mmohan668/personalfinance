package com.pf.common.dto.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseByCategory {
    private String categoryName;
    private BigDecimal amount = BigDecimal.ZERO;
}
