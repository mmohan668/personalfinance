package com.pf.common.dto.userManagement;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserPermissionViewDto {

    private Long id;
    private Long userId;
    private Long moduleId;
    private Long screenId;
    private Long actionId;
    private Boolean hasPermission;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private String moduleName;
    private String screenName;
    private String actionName;
}
