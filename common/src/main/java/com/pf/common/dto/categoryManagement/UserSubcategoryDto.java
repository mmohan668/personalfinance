package com.pf.common.dto.categoryManagement;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubcategoryDto {

    private Long id;

    private Long userCategoryId;

    private String subcategoryName;

    private String subcategoryDescription;

    private Boolean active;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;
}