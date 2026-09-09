package com.pf.common.entity.categoryManagement;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "v_subcategories_view")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcategoryView {

    @Id
    private Long id; // usc.id is unique, safe to use as PK

    private Long userCategoryId;
    private String categoryType;
    private String categoryName;
    private String categoryDescription;
    private Long adminUserId;

    private String subcategoryName;
    private String subcategoryDescription;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
