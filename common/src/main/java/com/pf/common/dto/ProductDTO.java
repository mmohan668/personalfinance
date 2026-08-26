package com.pf.common.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProductDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer quantity;
}
