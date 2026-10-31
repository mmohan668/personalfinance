package com.pf.common.projection;

import java.math.BigDecimal;

public interface MonthlyTransactionTotalProjection {
    Integer getTransactionYear();
    Integer getTransactionMonth();
    String getTransactionType();
    BigDecimal getTotal();
}
