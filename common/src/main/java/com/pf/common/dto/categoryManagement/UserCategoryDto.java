package com.pf.common.dto.categoryManagement;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCategoryDto {

    public static final Map<String, String> FIELD_MAPPINGS = Map.of(
            "categoryType", "referenceValue.referenceCode"
    );

    private Long id;

    private Long userId;

    private Long categoryTypeId;

    private String categoryType;

    private String categoryName;

    private String categoryDescription;

    private Boolean active;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime updatedAt;

}