package com.pf.common.dto.bulkUpload;

import com.pf.common.dto.BaseDto;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceUploadStatusDto extends BaseDto {
    public static final Map<String, String> FIELD_MAPPINGS = Map.of(
            USER_ID, "uploadedBy.adminUser.id",
            "uploadedBy", "uploadedBy.username"
    );

    private Long id;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String fileName;
    private String status;
    private String categorySubcategorySuggestion;
    private LocalDate uploadedDate;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}
