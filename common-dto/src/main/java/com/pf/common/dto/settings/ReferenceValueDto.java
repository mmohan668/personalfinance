package com.pf.common.dto.settings;

import com.pf.common.dto.BaseDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ReferenceValueDto extends BaseDto {

    public static final Map<String, String> FIELD_MAPPINGS = Map.of(
            REF_OBJ_NAME, REFERENCE_OBJECT_REF_OBJ_NAME,
            CREATED_BY, CREATED_BY_USERNAME,
            UPDATED_BY, UPDATED_BY_USERNAME,
            USER_ID, USER__ID
    );

    private Long id;
    private Long refObjNameId;
    private String refObjName;
    private String referenceCode;
    private String referenceCodeDescription;
    private boolean active;
    private String referenceCode2;
    private String referenceCode3;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}