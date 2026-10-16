package com.pf.common.dto.gp;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SearchCriteria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private List<GridSort> sortList = new ArrayList<>();
    private List<GridFilter> filterList = new ArrayList<>();
    private int skip;
    private int take;
    private boolean loadAllData;
    private String gridName;
    private List<String> fetchPaths = new ArrayList<>();
    private Map<String, String> FIELD_MAPPINGS;


    public void addFilter(String field, String operator, String value) {
        addFilter(field, operator, value, null, null);
    }

    public void addFilter(String field, String operator, String value, String valueTo) {
        addFilter(field, operator, value, valueTo, null);
    }

    public void addFilter(String field, String operator, List<?> values) {
        addFilter(field, operator, null, null, values);
    }

    public void addFilter(String field, String operator, String value, String valueTo, List<?> values) {
        filterList.add(new GridFilter(field, operator, value, valueTo, values));
    }

    public void addFilter(String field, String operator) {
        addFilter(field, operator, null, null);
    }

    public void addSort(String field, String order) {
        sortList.add(new GridSort(field, order));
    }

}
