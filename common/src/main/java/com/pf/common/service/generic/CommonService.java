package com.pf.common.service.generic;

import com.pf.common.dto.generic.SystemConfigValue;
import com.pf.common.dto.generic.SchedulerUserConfig;
import com.pf.common.entity.settings.SystemConfig;
import com.pf.common.repository.setting.SystemConfigRepository;
import com.pf.common.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.pf.common.enums.AppEnums.SystemConfigName.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonService {
    private final SystemConfigRepository systemConfigRepository;
    private final UserRepository userRepository;

    public List<SchedulerUserConfig> fetchSchedulerUserConfigs() {
        List<Long> adminUserIds = userRepository.findAllAdminUserIds();
        if (adminUserIds.isEmpty()) {
            return List.of();
        }

        return systemConfigRepository.findSchedulerSettingsByUserIdIn(adminUserIds)
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(SystemConfig::getUserId))
                .entrySet()
                .stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    String countryCode = null;
                    String countryName = null;
                    String stateCode = null;
                    String stateName = null;
                    String timeZone = null;
                    for (SystemConfig setting : entry.getValue()) {
                        String value = setting.getConfigValue().getReferenceCode();
                        String description = setting.getConfigValue().getReferenceCodeDescription();
                        switch (setting.getConfigName().trim().toLowerCase(java.util.Locale.ROOT)) {
                            case "country" -> {
                                countryCode = value;
                                countryName = description;
                            }
                            case "state" -> {
                                stateCode = value;
                                stateName = description;
                            }
                            case "time zone" -> timeZone = value;
                            default -> throw new IllegalStateException(
                                    "Unexpected scheduler setting: " + setting.getConfigName()
                            );
                        }
                    }
                    if (countryCode == null || countryName == null ||
                            stateCode == null || stateName == null || timeZone == null) {
                        log.warn("Skipping scheduler configuration for admin user {}: settings are incomplete", userId);
                        return null;
                    }
                    return new SchedulerUserConfig(
                            userId,
                            countryCode,
                            countryName,
                            stateCode,
                            stateName,
                            timeZone
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public SystemConfigValue fetchSystemConfig() {
        SystemConfigValue systemConfigValue = new SystemConfigValue();
        List<SystemConfig> systemConfigList = systemConfigRepository.findAll();
        systemConfigList.forEach(systemConfig -> {
            if (systemConfig.getConfigName().equalsIgnoreCase(CURRENCY.getValue())) {
                systemConfigValue.setCurrencyCode(systemConfig.getConfigValue().getReferenceCode());
                systemConfigValue.setLocale(systemConfig.getConfigValue().getReferenceCode2());
            }
            if (systemConfig.getConfigName().equalsIgnoreCase(DATE_FORMAT.getValue())) {
                systemConfigValue.setDateFormat(systemConfig.getConfigValue().getReferenceCode());
            }
            if (systemConfig.getConfigName().equalsIgnoreCase(COUNTRY.getValue())) {
                systemConfigValue.setCountryCode(systemConfig.getConfigValue().getReferenceCode());
            }
            if (systemConfig.getConfigName().equalsIgnoreCase(STATE.getValue())) {
                systemConfigValue.setStateCode(systemConfig.getConfigValue().getReferenceCode());
            }
            if (systemConfig.getConfigName().equalsIgnoreCase(TIME_ZONE.getValue())) {
                systemConfigValue.setTimeZone(systemConfig.getConfigValue().getReferenceCode());
            }
        });
        return systemConfigValue;
    }
}
