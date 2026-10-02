package com.pf.common.service.generic;

import com.pf.common.dto.generic.SystemConfigValue;
import com.pf.common.entity.settings.ReferenceValue;
import com.pf.common.entity.settings.SystemConfig;
import com.pf.common.repository.setting.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommonService {
    public static final String CURRENCY = "Currency";
    private final SystemConfigRepository systemConfigRepository;

    public SystemConfigValue fetchSystemConfig() {
        SystemConfigValue systemConfigValue = new SystemConfigValue();
        List<SystemConfig> systemConfigList = systemConfigRepository.findAll();
        systemConfigList.forEach(systemConfig -> {
            if (systemConfig.getConfigName().equalsIgnoreCase(CURRENCY)) {
                ReferenceValue currency = systemConfig.getConfigValue();
                systemConfigValue.setCurrencyCode(currency.getReferenceCode());
                systemConfigValue.setLocale(currency.getReferenceCode2());
                systemConfigValue.setDateFormat(currency.getReferenceCode3());
            }
        });
        return systemConfigValue;
    }
}
