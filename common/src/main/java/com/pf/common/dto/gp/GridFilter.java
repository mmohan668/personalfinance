package com.pf.common.dto.gp;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class GridFilter implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String field;
    private String operator;
    private String value;
    private String valueTo;
}
