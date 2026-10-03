package com.pf.common.entity.generic;

import com.pf.common.entity.userManagement.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"templateHeaders", "templateAdditionalSheets"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "template_name")
public class TemplateName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Column(name = "sheet_name", nullable = false)
    private String sheetName;

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

    @OneToMany(mappedBy = "templateName", fetch = FetchType.LAZY)
    private Set<TemplateHeader> templateHeaders;

    @OneToMany(mappedBy = "templateName", fetch = FetchType.LAZY)
    private Set<TemplateAdditionalSheet> templateAdditionalSheets;
}
