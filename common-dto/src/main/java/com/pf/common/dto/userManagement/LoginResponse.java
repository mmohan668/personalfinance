package com.pf.common.dto.userManagement;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserProfile userProfile,
        Boolean mustChangePassword,
        List<AppPermission> permissions
) {
}
