package com.pf.common.entity.generic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String message;
    private Object data;
}
