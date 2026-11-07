package com.pf.common.entity.userManagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Immutable
@Table(name = "vw_user_permissions")
public class UserPermissionView {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "module_id")
    private Long moduleId;

    @Column(name = "screen_id")
    private Long screenId;

    @Column(name = "action_id")
    private Long actionId;

    @Column(name = "has_permission")
    private Boolean hasPermission;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "screen_name")
    private String screenName;

    @Column(name = "action_name")
    private String actionName;

    @Column(name = "module_display_order")
    private Integer moduleDisplayOrder;

    @Column(name = "screen_display_order")
    private Integer screenDisplayOrder;

    @Column(name = "action_display_order")
    private Integer actionDisplayOrder;
}
