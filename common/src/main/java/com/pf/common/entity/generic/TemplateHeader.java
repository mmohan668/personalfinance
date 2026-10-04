package com.pf.common.entity.generic;

import com.pf.common.entity.userManagement.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@ToString(exclude = {"templateName"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "template_header")
public class TemplateHeader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "db_column_name", nullable = false)
    private String dbColumnName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_name_id", nullable = false)
    private TemplateName templateName;

    @Column(name = "header_name", nullable = false)
    private String headerName;

    @Column(name = "column_index", nullable = false)
    private Integer columnIndex;

    @Column(name = "data_type", nullable = false)
    private String dataType;

    @Column(name = "required")
    private boolean required;

    @Column(name = "min_length")
    private Integer minLength;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "regex_pattern")
    private String regexPattern;

    @Column(name = "db_validation_type")
    private String dbValidationType;

    @Column(name = "db_validation_query")
    private String dbValidationQuery;

    @Column(name = "db_validation_columns")
    private String dbValidationColumns;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
