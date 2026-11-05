package com.pf.common.enums;

import lombok.Getter;

public enum EntityName {
    REFERENCE_OBJECT("referenceObject"),
    REFERENCE_VALUE("userReferenceValue"),
    USER_CATEGORY("userCategory"),
    USER_SUBCATEGORY("userSubCategory"),
    USER("user");
    @Getter
    private final String value;

    EntityName(String value) {
        this.value = value;
    }
}
