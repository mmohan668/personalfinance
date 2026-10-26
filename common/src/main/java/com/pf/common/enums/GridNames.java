package com.pf.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum GridNames {
    CATEGORIES_GRID("Categories"),
    SUBCATEGORIES_GRID("Subcategories"),
    REFERENCE_OBJECTS_GRID("Reference Objects"),
    REFERENCE_VALUES_GRID("Reference Values"),
    EXPENSES_GRID("Expense Transactions"),
    INCOMES_GRID("Income Transactions"),
    INVESTMENTS_GRID("Investment Transactions"),
    TRANSFERS_GRID("Transfer Transactions"),
    ALL_TRANSACTIONS_GRID("All Financial Transactions"),
    SYSTEM_CONFIG("System Config"),
    BULK_UPLOADS_STATUS("Bulk Uploads Status"),
    USERS_GRID("Users");
    @Getter
    private final String value;

}
