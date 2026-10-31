package com.pf.common.entity.userManagement;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "users_password_history",
        indexes = {
                @Index(
                        name = "idx_password_history_user_changed_at",
                        columnList = "user_id, changed_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "password_history_id")
    private Long passwordHistoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_users_password_history_user")
    )
    private User user;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 10)
    @Builder.Default
    private PasswordActionType actionType = PasswordActionType.CHANGE;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    @Column(
            name = "changed_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Instant changedAt;

    public enum PasswordActionType {
        INITIAL,
        CHANGE,
        RESET
    }
}