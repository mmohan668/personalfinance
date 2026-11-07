package com.pf.common.dto.userManagement;

public record AppPermission(
        String actionName,
        String moduleName,
        String screenName,
        Boolean hasPermission
) {
}
