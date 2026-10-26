package com.pf.common.entity.userManagement;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(
                        name = "idx_users_admin_user_id",
                        columnList = "admin_user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id")
    private User adminUser;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "display_name", nullable = false, unique = true, length = 100)
    private String displayName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Formula("CASE WHEN admin_user_id = id THEN true ELSE false END")
    private Boolean isAdminUser;

    @PrePersist
    public void prePersist() {
        this.active = false;
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}