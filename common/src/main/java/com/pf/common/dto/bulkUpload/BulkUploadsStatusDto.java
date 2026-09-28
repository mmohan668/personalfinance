package com.pf.common.dto.bulkUpload;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkUploadsStatusDto {
    public static final Map<String, String> FIELD_MAPPINGS = Map.of(
            "createdBy", "createdBy.username"
    );
    private Long id;
    private String uploadType;
    private String uploadedFile;
    private String status;
    private String errorFile;
    private Long createdBy;
    private LocalDateTime createdAt;
}
