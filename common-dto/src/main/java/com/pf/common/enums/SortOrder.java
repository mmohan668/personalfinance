package com.pf.common.enums;

import lombok.Getter;

@Getter
public enum SortOrder {
    ASC("asc"),
    DESC("desc");
    
    private final String value;

    SortOrder(String value) {
        this.value = value;
    }
}
