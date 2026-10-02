package com.pf.common.entity.validator;

import com.pf.common.enums.AppEnums;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "excel_validator_metadata",
        indexes = {
                @Index(
                        name = "ux_excel_validator_metadata",
                        columnList = "upload_type, sheet_name, column_name",
                        unique = true
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelValidatorMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upload_type", nullable = false, length = 100)
    private String uploadType;

    @Column(name = "sheet_name", nullable = false, length = 100)
    private String sheetName;

    @Column(name = "column_name", nullable = false, length = 100)
    private String columnName;

    @Column(name = "column_index", nullable = false)
    private Integer columnIndex;

    @Column(name = "data_type", nullable = false, length = 20)
    private String dataType;

    @Column(name = "required", nullable = false)
    @Builder.Default
    private Boolean required = false;

    @Column(name = "min_length")
    private Integer minLength;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "regex_pattern")
    private String regexPattern;

    @Column(name = "db_validation_type", length = 50)
    private String dbValidationType;

    @Column(name = "db_validation_query", columnDefinition = "TEXT")
    private String dbValidationQuery;

    @Column(name = "db_validation_columns", columnDefinition = "TEXT")
    private String dbValidationColumns;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
