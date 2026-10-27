package com.pf.common.projection;

import java.math.BigDecimal;

public interface ExpenseByCategoryProjection {
    String getCategoryName();

    BigDecimal getAmount();
}
