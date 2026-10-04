package com.pf.common.enums;

import lombok.Getter;

public class AppEnums {

    public enum DbValidationType {
        DB_EXISTS, DB_DUPLICATE
    }

    public enum DataType {
        TEXT, P_NUMBER, NUMBER, DATE, AMOUNT
    }

    @Getter
    public enum SystemConfigName {
        CURRENCY("Currency");
        private final String value;

        SystemConfigName(String value) {
            this.value = value;
        }
    }

    public enum DataReference {
        REFERENCE_OBJECT, TRANSACTION_TYPE, CATEGORY, SUB_CATEGORY, LOCATION
    }

    public enum TemplateName {
        REFERENCE_VALUES, CATEGORIES, SUBCATEGORIES, FINANCIAL_TRANSACTIONS
    }

}
