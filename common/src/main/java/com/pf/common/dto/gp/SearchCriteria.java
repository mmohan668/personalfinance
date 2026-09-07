package com.pf.common.dto.gp;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class SearchCriteria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private List<GridSort> sortList;
    private List<GridFilter> filterList;
    private int skip;
    private int take;
    private boolean loadAllData;
    private String gridName;
}
