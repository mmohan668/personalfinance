package com.pf.common.enums;

import lombok.Getter;

@Getter
public enum FilterOperator {
    IS_NULL("isNull"),
    IS_NOT_NULL("isNotNull"),
    EQUALS("equals"),
    NOT_EQUALS("notEquals"),
    CONTAINS("contains"),
    NOT_CONTAINS("notContains"),
    STARTS_WITH("startsWith"),
    ENDS_WITH("endsWith"),
    GREATER_THAN("gt"),
    GREATER_THAN_OR_EQUALS("gte"),
    LESS_THAN("lt"),
    LESS_THAN_OR_EQUALS("lte"),
    BETWEEN("between"),
    IN("in");

    private final String value;

    FilterOperator(String value) {
        this.value = value;
    }

}
