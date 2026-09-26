package com.pf.common.entity.settings;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reference_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferenceValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ref_obj_name_id", nullable = false)
    private ReferenceObject referenceObject;

    @NotBlank
    @Size(max = 100)
    @Column(name = "reference_code", nullable = false, length = 100)
    private String referenceCode;

    @NotBlank
    @Size(max = 200)
    @Column(name = "reference_code_description", nullable = false, length = 200)
    private String referenceCodeDescription;

    @Size(max = 100)
    @Column(name = "reference_code_2", length = 100)
    private String referenceCode2;

    @Size(max = 100)
    @Column(name = "reference_code_3", length = 100)
    private String referenceCode3;

    @NotBlank
    @Size(max = 100)
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Size(max = 100)
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}