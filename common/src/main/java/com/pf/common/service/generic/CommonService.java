package com.pf.common.service.generic;

import com.pf.common.dto.generic.SystemConfigValue;
import com.pf.common.entity.settings.SystemConfig;
import com.pf.common.repository.setting.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.pf.common.enums.AppEnums.SystemConfigName.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommonService {
    private final SystemConfigRepository systemConfigRepository;

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
        });
        return systemConfigValue;
    }
}
