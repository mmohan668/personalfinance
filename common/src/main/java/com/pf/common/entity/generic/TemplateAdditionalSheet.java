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
@Table(name = "template_additional_sheet")
public class TemplateAdditionalSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_name_id", nullable = false)
    private TemplateName templateName;

    @Column(name = "sheet_name", nullable = false)
    private String sheetName;

    @Column(name = "data_reference", nullable = false)
    private String dataReference;

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
