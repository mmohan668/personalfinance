package com.pf.common.dto.userManagement;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserProfile userProfile
) {
}

