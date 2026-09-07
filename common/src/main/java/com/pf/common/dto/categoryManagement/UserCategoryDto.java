package com.pf.common.dto.categoryManagement;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCategoryDto {

    private Long id;

    private Long userId;

    private String transactionType;

    private String categoryName;

    private String categoryDescription;

    private Boolean active;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

}