package com.pf.common.dto.categoryManagement;

import com.pf.common.dto.BaseDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCategoryDto extends BaseDto {

    public static final Map<String, String> FIELD_MAPPINGS = Map.of(
            TRANSACTION_TYPE, REFERENCE_VALUE_REFERENCE_CODE
    );

    private Long id;
    private Long userId;
    private Long transactionTypeId;
    private String transactionType;
    private String categoryName;
    private String categoryDescription;
    private Boolean active;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

}