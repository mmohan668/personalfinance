package com.pf.common.projection;

import java.math.BigDecimal;

public interface TransactionTotalProjection {
    BigDecimal getTotal();
    String getTransactionType();
}
