package com.pf.common.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Data
@Entity(name = "products")
public class Product {
    @Id
    private BigInteger id;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer quantity;
    private LocalDate createdDate;
    private boolean active;
    private LocalDate lastUpdated;
    private boolean discontinued;
    private String sku;
    private String description;
    private Double rating;
}
