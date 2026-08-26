package com.pf.common.dto.gp;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GridResult implements Serializable {
    private List<?> recordDetails;
    private int totalRecords;
}
