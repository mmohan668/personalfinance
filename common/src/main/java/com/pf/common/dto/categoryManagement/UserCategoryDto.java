package com.pf.common.dto.categoryManagement;

import com.pf.common.enums.TransactionType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCategoryDto {

    private Long id;

    private Long userId;

    private TransactionType transactionType;

    private String categoryName;

    private String categoryDescription;

    private Boolean active;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private List<UserSubcategoryDto> subcategories;
}