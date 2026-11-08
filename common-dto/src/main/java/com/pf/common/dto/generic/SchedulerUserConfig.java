package com.pf.common.dto.generic;

public record SchedulerUserConfig(
        Long userId,
        String countryCode,
        String countryName,
        String stateCode,
        String stateName,
        String timeZone
) {
}
