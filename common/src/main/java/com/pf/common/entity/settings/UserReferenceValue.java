package com.pf.common.entity.settings;

import com.pf.common.entity.userManagement.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_reference_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReferenceValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Size(max = 100)
    @Column(name = "reference_code_2", length = 100)
    private String referenceCode2;

    @Size(max = 100)
    @Column(name = "reference_code_3", length = 100)
    private String referenceCode3;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deleted_by", nullable = false)
    private User deletedBy;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        active = true;
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}