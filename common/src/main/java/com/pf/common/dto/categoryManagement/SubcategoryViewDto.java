package com.pf.common.dto.categoryManagement;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcategoryViewDto {
    private Long id;
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
