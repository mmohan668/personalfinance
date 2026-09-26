package com.pf.common.dto.settings;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferenceValueDto {

    public static final Map<String, String> FIELD_MAPPINGS = Map.of(
            "refObjName", "referenceObject.refObjName"
    );

    private Long id;
    private Long refObjNameId;
    private String refObjName;
    private String referenceCode;
    private String referenceCodeDescription;
    private String referenceCode2;
    private String referenceCode3;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}