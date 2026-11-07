package com.pf.common.dto.userManagement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionUpdateRequest {
    private Long userId;
    private List<UserPermissionUpdate> permissions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPermissionUpdate {
        private Long id;
        private Boolean hasPermission;
    }
}
