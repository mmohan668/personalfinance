package com.pf.common.dto.gp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridSort implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String field;
    private String order;
}
