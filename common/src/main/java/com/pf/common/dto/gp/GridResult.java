package com.pf.common.dto.gp;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class GridResult implements Serializable {
    private List<?> recordDetails;
    private long totalRecords;
}
