package com.pf.common.dto.bulkUpload;

import com.pf.common.dto.BaseDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkUploadsStatusDto extends BaseDto {
    public static final Map<String, String> FIELD_MAPPINGS = Map.of(
            CREATED_BY, CREATED_BY_USERNAME
    );
    private Long id;
    private String uploadType;
    private String uploadedFile;
    private String status;
    private String errorFile;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
}
