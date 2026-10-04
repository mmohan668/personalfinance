package com.pf.common.dto.generic;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class TemplateRowData {
    private final Map<String, Object> values;

    public TemplateRowData() {
        this.values = new HashMap<>();
    }

    public void put(String dbColumnName, Object value) {
        values.put(dbColumnName, value);
    }

}
