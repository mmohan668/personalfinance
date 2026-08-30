package com.pf.common.dto.gp;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class GridColumnDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String field;
    private String header;
    private String type;
    private boolean sortable;
    private String defaultSortOrder;
    private boolean filterable;
    private String defaultFilterOperator;
    private String width;
    private String align;
    private boolean visible;
    private int visibleIndex;
    private String cellTemplate;
}
