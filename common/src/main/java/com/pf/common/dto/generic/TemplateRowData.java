package com.pf.common.dto.generic;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class TemplateRowData {
    private final Map<String, Object> values;

    public TemplateRowData() {
        this.values = new HashMap<>();
    }

    public void put(String dbColumnName, Object value) {
        values.put(dbColumnName, value);
    }

}
