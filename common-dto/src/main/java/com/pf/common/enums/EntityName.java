package com.pf.common.enums;

import lombok.Getter;

public enum EntityName {
    REFERENCE_VALUE("referenceValue"),
    USER_CATEGORY("userCategory"),
    USER_SUBCATEGORY("userSubCategory"),
    USER("user");
    @Getter
    private final String value;

    EntityName(String value) {
        this.value = value;
    }
}
